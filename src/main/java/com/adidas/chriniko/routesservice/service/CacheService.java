package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.codahale.metrics.Meter;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Log4j2

@Service
public class CacheService {

    private final RedisTemplate<CityInfo, RouteInfo> redisTemplate;
    private final Meter cacheHitFindByCityInfo;
    private final Meter cacheMissFindByCityInfo;

    @Autowired
    public CacheService(RedisTemplate<CityInfo, RouteInfo> redisTemplate,
                        Meter cacheHitFindByCityInfo,
                        Meter cacheMissFindByCityInfo) {
        this.redisTemplate = redisTemplate;
        this.cacheHitFindByCityInfo = cacheHitFindByCityInfo;
        this.cacheMissFindByCityInfo = cacheMissFindByCityInfo;
    }

    public Mono<RouteInfo> get(CityInfo cityInfo) {
        return Mono.create(sink -> {
            try {
                RouteInfo result = redisTemplate.opsForValue().get(cityInfo);

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

    void upsert(CityInfo input, RouteInfo output) {
        Mono
                .<Pair<CityInfo, RouteInfo>>create(sink -> {
                    try {
                        redisTemplate.opsForValue().set(input, output);
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
