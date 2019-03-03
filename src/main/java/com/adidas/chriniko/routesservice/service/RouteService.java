package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Log4j2

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final TransactionTemplate transactionTemplate;
    private final CacheService cacheService;

    @Autowired
    public RouteService(RouteRepository routeRepository,
                        TransactionTemplate transactionTemplate,
                        CacheService cacheService) {
        this.routeRepository = routeRepository;
        this.transactionTemplate = transactionTemplate;
        this.cacheService = cacheService;
    }

    public Mono<RouteInfo> find(CityInfo cityInfo) {
        return cacheService
                .get(cityInfo)
                .subscribeOn(Schedulers.parallel())
                .switchIfEmpty(_find(cityInfo));
    }

    public Mono<RouteInfo> create(RouteInfo routeInfo) {
        return Mono
                .<Optional<RouteEntity>>create(sink -> {
                    try {
                        log.debug("will search if entry exists with info: {}", routeInfo);

                        Optional<RouteEntity> result = routeRepository.find(
                                routeInfo.getCity().getName(), routeInfo.getCity().getCountry(),
                                routeInfo.getDestinyCity().getName(), routeInfo.getDestinyCity().getCountry()
                        );
                        sink.success(result);

                    } catch (Exception e) {
                        log.error("error occurred during find route info operation", e);
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .map(result -> {
                            if (result.isPresent()) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "record already exists with info: " + result.get());
                            } else {
                                log.debug("will store new entry with info: {}", routeInfo);

                                RouteEntity routeEntity = new RouteEntity();

                                updateState(routeInfo, routeEntity);

                                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                    @Override
                                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                        routeRepository.insert(routeEntity);
                                    }
                                });
                                return routeInfo;
                            }
                        }
                );
    }

    public Mono<RouteInfo> update(String routeId, RouteInfo routeInfo) {
        return searchById(routeId)
                .map(result -> {
                    if (!result.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no record exists with id: " + routeId);
                    } else {
                        log.debug("will update entry with id: {} and info: {}", routeId, routeInfo);

                        RouteEntity routeEntity = result.get();

                        cacheService.remove(routeId);
                        cacheService.remove(extractOrigin(routeEntity));

                        updateState(routeInfo, routeEntity);

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                routeRepository.update(routeEntity);
                            }
                        });

                        cacheService.upsert(routeId, routeInfo);
                        cacheService.upsert(extractOrigin(routeEntity), routeInfo);

                        return routeInfo;
                    }
                });
    }

    public Mono<RouteInfo> find(String routeId) {
        return cacheService
                .get(routeId)
                .subscribeOn(Schedulers.parallel())
                .switchIfEmpty(
                        searchById(routeId)
                                .map(result -> {
                                    if (!result.isPresent()) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no record exists with id: " + routeId);
                                    } else {
                                        RouteInfo routeInfo = map(result.get());
                                        cacheService.upsert(routeId, routeInfo);
                                        return routeInfo;
                                    }
                                })
                );
    }

    public Mono<RouteInfo> delete(String routeId) {
        return searchById(routeId)
                .map(result -> {
                    if (!result.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no record exists with id: " + routeId);
                    } else {
                        RouteEntity routeEntity = result.get();

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                routeRepository.delete(routeEntity);
                            }
                        });

                        CityInfo cityInfo = extractOrigin(routeEntity);

                        cacheService.remove(cityInfo);
                        cacheService.remove(routeEntity.getId());

                        return map(result.get());
                    }
                });
    }

    private Mono<RouteInfo> _find(CityInfo cityInfo) {
        return Mono
                .<Optional<RouteEntity>>create(sink -> {
                    try {
                        Optional<RouteEntity> result
                                = routeRepository.find(cityInfo.getName(), cityInfo.getCountry());
                        sink.success(result);

                    } catch (Exception e) {
                        log.error("error occurred during find city info operation", e);
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .map(routeEntity -> {
                    log.debug("will transform fetched entry: {}", routeEntity);
                    return routeEntity
                            .map(entity -> {
                                RouteInfo routeInfo = map(entity);
                                cacheService.upsert(cityInfo, routeInfo);
                                return routeInfo;
                            })
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no record exists with: " + cityInfo));
                });
    }

    private Mono<Optional<RouteEntity>> searchById(String routeId) {
        return Mono
                .<Optional<RouteEntity>>create(sink -> {
                    try {
                        log.debug("will search if entry exists with id: {}", routeId);

                        Optional<RouteEntity> result = routeRepository.find(routeId);

                        sink.success(result);

                    } catch (Exception e) {
                        log.error("error occurred during find route info operation", e);
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.parallel());
    }

    private void updateState(RouteInfo routeInfo, RouteEntity routeEntity) {
        routeEntity.setOriginCityName(routeInfo.getCity().getName());
        routeEntity.setOriginCountry(routeInfo.getCity().getCountry());

        routeEntity.setDestinyCityName(routeInfo.getDestinyCity().getName());
        routeEntity.setDestinyCountry(routeInfo.getDestinyCity().getCountry());

        routeEntity.setDepartureTime(routeInfo.getDepartureTime());
        routeEntity.setArrivalTime(routeInfo.getArrivalTime());

        routeInfo.setId(routeEntity.getId());
    }

    private RouteInfo map(RouteEntity entity) {
        CityInfo originCityInfo = extractOrigin(entity);
        CityInfo destinyCityInfo = extractDestiny(entity);

        return new RouteInfo(
                entity.getId(),
                originCityInfo,
                destinyCityInfo,
                entity.getDepartureTime(),
                entity.getArrivalTime()
        );
    }

    private CityInfo extractOrigin(RouteEntity routeEntity) {
        return new CityInfo(routeEntity.getOriginCityName(), routeEntity.getOriginCountry());
    }

    private CityInfo extractDestiny(RouteEntity routeEntity) {
        return new CityInfo(routeEntity.getDestinyCityName(), routeEntity.getDestinyCountry());
    }
}
