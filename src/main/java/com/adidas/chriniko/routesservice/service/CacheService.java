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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j2

@Service
public class CacheService implements ApplicationListener<ContextRefreshedEvent> {

    private final RedisTemplate<CityInfo, List<RouteInfo>> cityInfoToRouteInfos;
    private final RedisTemplate<String, RouteInfo> routeIdToRouteInfo;

    private final Meter cacheHitFindByCityInfo;
    private final Meter cacheMissFindByCityInfo;
    private final Meter cacheHitFindByRouteId;
    private final Meter cacheMissFindByRouteId;


    @Autowired
    public CacheService(RedisTemplate<CityInfo, List<RouteInfo>> cityInfoToRouteInfos,
                        RedisTemplate<String, RouteInfo> routeIdToRouteInfo,
                        Meter cacheHitFindByCityInfo,
                        Meter cacheMissFindByCityInfo,
                        Meter cacheHitFindByRouteId,
                        Meter cacheMissFindByRouteId) {
        this.cityInfoToRouteInfos = cityInfoToRouteInfos;
        this.routeIdToRouteInfo = routeIdToRouteInfo;
        this.cacheHitFindByCityInfo = cacheHitFindByCityInfo;
        this.cacheMissFindByCityInfo = cacheMissFindByCityInfo;
        this.cacheHitFindByRouteId = cacheHitFindByRouteId;
        this.cacheMissFindByRouteId = cacheMissFindByRouteId;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Set<CityInfo> cityInfoKeys = cityInfoToRouteInfos.opsForValue().getOperations().keys(new CityInfo("*", "*"));
        log.debug("will clear redis [cache(CityInfo) ---> RouteInfo], keys: {}", cityInfoKeys);
        cityInfoToRouteInfos.opsForValue().getOperations().delete(cityInfoKeys);

        Set<String> routeIdKeys = routeIdToRouteInfo.opsForValue().getOperations().keys("*");
        log.debug("will clear redis [cache(routeId:String) ---> RouteInfo], keys: {}", routeIdKeys);
        routeIdToRouteInfo.opsForValue().getOperations().delete(routeIdKeys);
    }

    Mono<List<RouteInfo>> get(CityInfo cityInfo) {
        return Mono
                .<List<RouteInfo>>create(sink -> {
                    try {
                        List<RouteInfo> results = cityInfoToRouteInfos.opsForValue().get(cityInfo);

                        if (results != null) {
                            log.debug("cache hit(cityInfo), result: {}", results);
                            cacheHitFindByCityInfo.mark();
                        } else {
                            log.debug("cache miss(cityInfo), cityInfo: {}", cityInfo);
                            cacheMissFindByCityInfo.mark();
                        }

                        sink.success(results);
                    } catch (Exception e) {
                        log.error("cache get(cityInfo) operation failed", e);
                        sink.error(e);
                    }
                })
                .retryBackoff(3, Duration.ofMillis(5), Duration.ofMillis(12));
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
                        Boolean removed = cityInfoToRouteInfos.opsForValue().getOperations().delete(cityInfo);
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

    Mono<Pair<CityInfo, List<RouteInfo>>> upsert(CityInfo cityInfo, RouteInfo routeInfo) {
        List<RouteInfo> routeInfos = new ArrayList<>();
        routeInfos.add(routeInfo);
        return this.upsert(cityInfo, routeInfos);
    }

    Mono<Pair<CityInfo, List<RouteInfo>>> upsert(CityInfo cityInfo, List<RouteInfo> routeInfos) {
        return Mono
                .<Pair<CityInfo, List<RouteInfo>>>create(sink -> {
                    try {
                        List<RouteInfo> existingRouteInfos = cityInfoToRouteInfos.opsForValue().get(cityInfo);

                        if (existingRouteInfos != null) { // Note: if entries already exists then...

                            existingRouteInfos.removeAll(routeInfos);
                            existingRouteInfos.addAll(routeInfos);

                            cityInfoToRouteInfos.opsForValue().set(cityInfo, existingRouteInfos);

                            sink.success(Pair.with(cityInfo, existingRouteInfos));

                        } else {

                            cityInfoToRouteInfos.opsForValue().set(cityInfo, routeInfos);
                            sink.success(Pair.with(cityInfo, routeInfos));
                        }

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
