package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.codahale.metrics.Meter;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;

@Log4j2

@Service
public class CacheService implements ApplicationListener<ContextRefreshedEvent> {

    private final RedisTemplate<CityInfo, RouteInfo> cityInfoToRouteInfo;
    private final RedisTemplate<String, RouteInfo> routeIdToRouteInfo;

    private final Meter cacheHitFindByCityInfo;
    private final Meter cacheMissFindByCityInfo;

    @Autowired
    private Meter cacheHitFindByRouteId;

    @Autowired
    private Meter cacheMissFindByRouteId;


    @Autowired
    public CacheService(RedisTemplate<CityInfo, RouteInfo> cityInfoToRouteInfo,
                        RedisTemplate<String, RouteInfo> routeIdToRouteInfo,
                        Meter cacheHitFindByCityInfo,
                        Meter cacheMissFindByCityInfo) {
        this.cityInfoToRouteInfo = cityInfoToRouteInfo;
        this.routeIdToRouteInfo = routeIdToRouteInfo;
        this.cacheHitFindByCityInfo = cacheHitFindByCityInfo;
        this.cacheMissFindByCityInfo = cacheMissFindByCityInfo;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Set<CityInfo> keys = cityInfoToRouteInfo.opsForValue().getOperations().keys(new CityInfo("*", "*"));
        log.debug("will clear redis [cache(CityInfo) ---> RouteInfo], keys: {}", keys);

        cityInfoToRouteInfo.opsForValue().getOperations().delete(keys);
    }

    public Mono<RouteInfo> get(CityInfo cityInfo) {
        return Mono.create(sink -> {
            try {
                RouteInfo result = cityInfoToRouteInfo.opsForValue().get(cityInfo);

                if (result != null) {
                    log.debug("cache hit, result: {}", result);
                    cacheHitFindByCityInfo.mark();
                } else {
                    log.debug("cache miss, cityInfo: {}", cityInfo);
                    cacheMissFindByCityInfo.mark();
                }

                sink.success(result);
            } catch (Exception e) {
                log.error("cache get operation failed", e);
                sink.error(e);
            }
        });
    }

    //TODO add cache support routId ---> RouteInfo

    void remove(CityInfo cityInfo) {
        Mono
                .create(sink -> {
                    try {
                        Boolean removed = cityInfoToRouteInfo.opsForValue().getOperations().delete(cityInfo);
                        sink.success(removed);
                    } catch (Exception e) {
                        log.error("cache remove operation failed", e);
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                        result -> log.debug("cache removal operation outcome: {}", result),
                        throwable -> log.warn("could not remove result from cache", throwable)
                );
    }

    void upsert(CityInfo input, RouteInfo output) {
        Mono
                .<Pair<CityInfo, RouteInfo>>create(sink -> {
                    try {
                        cityInfoToRouteInfo.opsForValue().set(input, output);
                        sink.success(Pair.with(input, output));
                    } catch (Exception e) {
                        log.error("cache upsert operation failed", e);
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                        result -> log.debug("stored successfully in cache, result: {}", result),
                        throwable -> log.warn("could not store result to cache", throwable)
                );

    }
}
