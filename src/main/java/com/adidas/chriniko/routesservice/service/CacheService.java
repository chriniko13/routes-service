package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.function.Consumer;

@Log4j2

@Service
public class CacheService {

    private final RedisTemplate<CityInfo, RouteInfo> redisTemplate;

    @Autowired
    public CacheService(RedisTemplate<CityInfo, RouteInfo> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<RouteInfo> get(CityInfo cityInfo) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(cityInfo));
    }

    public void upsert(CityInfo input, RouteInfo output) {

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
                        new Consumer<Pair<CityInfo, RouteInfo>>() {
                            @Override
                            public void accept(Pair<CityInfo, RouteInfo> o) {

                            }
                        },
                        throwable -> {

                        },
                        () -> {

                        }
                );

    }

}
