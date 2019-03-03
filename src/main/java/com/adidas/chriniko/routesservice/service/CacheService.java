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

import java.time.Duration;
import java.util.Set;

@Log4j2

@Service
public class CacheService implements ApplicationListener<ContextRefreshedEvent> {

    private final RedisTemplate<CityInfo, RouteInfo> cityInfoToRouteInfo;
    private final RedisTemplate<String, RouteInfo> routeIdToRouteInfo;

    private final Meter cacheHitFindByCityInfo;
    private final Meter cacheMissFindByCityInfo;
    private final Meter cacheHitFindByRouteId;
    private final Meter cacheMissFindByRouteId;


    @Autowired
    public CacheService(RedisTemplate<CityInfo, RouteInfo> cityInfoToRouteInfo,
                        RedisTemplate<String, RouteInfo> routeIdToRouteInfo,
                        Meter cacheHitFindByCityInfo,
                        Meter cacheMissFindByCityInfo,
                        Meter cacheHitFindByRouteId,
                        Meter cacheMissFindByRouteId) {
        this.cityInfoToRouteInfo = cityInfoToRouteInfo;
        this.routeIdToRouteInfo = routeIdToRouteInfo;
        this.cacheHitFindByCityInfo = cacheHitFindByCityInfo;
        this.cacheMissFindByCityInfo = cacheMissFindByCityInfo;
        this.cacheHitFindByRouteId = cacheHitFindByRouteId;
        this.cacheMissFindByRouteId = cacheMissFindByRouteId;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Set<CityInfo> cityInfoKeys = cityInfoToRouteInfo.opsForValue().getOperations().keys(new CityInfo("*", "*"));
        log.debug("will clear redis [cache(CityInfo) ---> RouteInfo], keys: {}", cityInfoKeys);
        cityInfoToRouteInfo.opsForValue().getOperations().delete(cityInfoKeys);

        Set<String> routeIdKeys = routeIdToRouteInfo.opsForValue().getOperations().keys("*");
        log.debug("will clear redis [cache(routeId:String) ---> RouteInfo], keys: {}", routeIdKeys);
        routeIdToRouteInfo.opsForValue().getOperations().delete(routeIdKeys);
    }

    Mono<RouteInfo> get(CityInfo cityInfo) {
        return Mono
                .create(sink -> {
                    try {
                        RouteInfo result = cityInfoToRouteInfo.opsForValue().get(cityInfo);

                        if (result != null) {
                            log.debug("cache hit(cityInfo), result: {}", result);
                            cacheHitFindByCityInfo.mark();
                        } else {
                            log.debug("cache miss(cityInfo), cityInfo: {}", cityInfo);
                            cacheMissFindByCityInfo.mark();
                        }

                        sink.success(result);
                    } catch (Exception e) {
                        log.error("cache get(cityInfo) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12))
                .ofType(RouteInfo.class);
    }

    Mono<RouteInfo> get(String routeId) {
        return Mono
                .<RouteInfo>create(sink -> {
                    try {
                        RouteInfo result = routeIdToRouteInfo.opsForValue().get(routeId);

                        if (result != null) {
                            log.debug("cache hit(routeId), result: {}", result);
                            cacheHitFindByRouteId.mark();
                        } else {
                            log.debug("cache miss(routeId), routeId: {}", routeId);
                            cacheMissFindByRouteId.mark();
                        }

                        sink.success(result);
                    } catch (Exception e) {
                        log.error("cache get(routeId) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
    }

    Mono<Boolean> remove(CityInfo cityInfo) {
        return Mono
                .<Boolean>create(sink -> {
                    try {
                        Boolean removed = cityInfoToRouteInfo.opsForValue().getOperations().delete(cityInfo);
                        sink.success(removed);
                    } catch (Exception e) {
                        log.error("cache remove(cityInfo) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
    }

    Mono<Boolean> remove(String routeId) {
        return Mono
                .<Boolean>create(sink -> {
                    try {
                        Boolean removed = routeIdToRouteInfo.opsForValue().getOperations().delete(routeId);
                        sink.success(removed);
                    } catch (Exception e) {
                        log.error("cache remove(routeId) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
    }

    Mono<Pair<CityInfo, RouteInfo>> upsert(CityInfo cityInfo, RouteInfo routeInfo) {
        return Mono
                .<Pair<CityInfo, RouteInfo>>create(sink -> {
                    try {
                        cityInfoToRouteInfo.opsForValue().set(cityInfo, routeInfo);
                        sink.success(Pair.with(cityInfo, routeInfo));
                    } catch (Exception e) {
                        log.error("cache upsert(cityInfo,routeInfo) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
    }

    Mono<Pair<String, RouteInfo>> upsert(String routeId, RouteInfo routeInfo) {
        return Mono
                .<Pair<String, RouteInfo>>create(sink -> {
                    try {
                        routeIdToRouteInfo.opsForValue().set(routeId, routeInfo);
                        sink.success(Pair.with(routeId, routeInfo));
                    } catch (Exception e) {
                        log.error("cache upsert(routeId,routeInfo) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
    }
}
