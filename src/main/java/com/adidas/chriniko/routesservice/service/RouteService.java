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
                .switchIfEmpty(_find(cityInfo))
                .subscribeOn(Schedulers.parallel());
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
                .publishOn(Schedulers.elastic())
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

                        updateState(routeInfo, routeEntity);

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                routeRepository.update(routeEntity);
                            }
                        });

                        return routeInfo;
                    }
                });
    }

    public Mono<RouteInfo> find(String routeId) {
        return searchById(routeId)
                .map(result -> {
                    if (!result.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no record exists with id: " + routeId);
                    } else {
                        RouteEntity routeEntity = result.get();

                        return new RouteInfo(
                                routeEntity.getId(),
                                new CityInfo(routeEntity.getOriginCityName(), routeEntity.getOriginCountry()),
                                new CityInfo(routeEntity.getDestinyCityName(), routeEntity.getDestinyCountry()),
                                routeEntity.getDepartureTime(),
                                routeEntity.getArrivalTime()
                        );
                    }
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
        CityInfo originCityInfo = new CityInfo(entity.getOriginCityName(), entity.getOriginCountry());
        CityInfo destinyCityInfo = new CityInfo(entity.getDestinyCityName(), entity.getDestinyCountry());

        return new RouteInfo(
                entity.getId(),
                originCityInfo,
                destinyCityInfo,
                entity.getDepartureTime(),
                entity.getArrivalTime()
        );
    }

}
