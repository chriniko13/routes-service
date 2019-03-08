package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.codahale.metrics.Meter;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceTest {

    @Mock
    private RedisTemplate<CityInfo, List<RouteInfo>> cityInfoToRouteInfos;
    @Mock
    private ValueOperations<CityInfo, List<RouteInfo>> cityInfoToRouteInfosValueOps;
    @Mock
    private RedisOperations<CityInfo, List<RouteInfo>> cityInfoRouteInfosRedisOperations;

    @Mock
    private RedisTemplate<String, RouteInfo> routeIdToRouteInfo;
    @Mock
    private ValueOperations<String, RouteInfo> routeIdToRouteInfoValueOps;
    @Mock
    private RedisOperations<String, RouteInfo> routeIdToRouteInfoRedisOperations;

    @Mock
    private Meter cacheHitFindByCityInfo;
    @Mock
    private Meter cacheMissFindByCityInfo;
    @Mock
    private Meter cacheHitFindByRouteId;
    @Mock
    private Meter cacheMissFindByRouteId;

    private CacheService cacheService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        cacheService = new CacheService(
                cityInfoToRouteInfos,
                routeIdToRouteInfo,
                cacheHitFindByCityInfo,
                cacheMissFindByCityInfo,
                cacheHitFindByRouteId,
                cacheMissFindByRouteId
        );
    }

    @Test
    public void get_routeid_works_as_expected_cache_hit_case() {

        // given
        String id = UUID.randomUUID().toString();
        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo result = new RouteInfo(id, originCityInfo, destinyCityInfo, departureTime, arrivalTime);

        String routeId = "xyz";
        when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);

        when(routeIdToRouteInfoValueOps.get(routeId))
                .thenReturn(result);

        // when - then
        StepVerifier
                .create(cacheService.get(routeId))
                .expectNext(result)
                .verifyComplete();

        verify(cacheHitFindByRouteId, times(1)).mark();
        verify(cacheMissFindByRouteId, times(0)).mark();

    }

    @Test
    public void get_routeid_works_as_expected_cache_miss_case() {

        // given
        String routeId = "xyz";
        when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);

        when(routeIdToRouteInfoValueOps.get(routeId))
                .thenReturn(null);

        // when - then
        StepVerifier
                .create(cacheService.get(routeId))
                .verifyComplete();

        verify(cacheHitFindByRouteId, times(0)).mark();
        verify(cacheMissFindByRouteId, times(1)).mark();

    }

    @Test
    public void get_cityinfo_works_as_expected_hit_case() {

        // given
        String id = UUID.randomUUID().toString();
        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo routeInfo = new RouteInfo(id, originCityInfo, destinyCityInfo, departureTime, arrivalTime);

        List<RouteInfo> result = Collections.singletonList(routeInfo);

        CityInfo cityInfo = new CityInfo("origin-name", "origin-country");

        when(cityInfoToRouteInfos.opsForValue())
                .thenReturn(cityInfoToRouteInfosValueOps);

        when(cityInfoToRouteInfosValueOps.get(cityInfo))
                .thenReturn(result);


        // when - then
        StepVerifier
                .create(cacheService.get(cityInfo))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    public void get_cityinfo_works_as_expected_miss_case() {

        // given
        CityInfo cityInfo = new CityInfo("origin-name", "origin-country");

        when(cityInfoToRouteInfos.opsForValue())
                .thenReturn(cityInfoToRouteInfosValueOps);

        when(cityInfoToRouteInfosValueOps.get(cityInfo))
                .thenReturn(null);

        // when - then
        StepVerifier
                .create(cacheService.get(cityInfo))
                .verifyComplete();
    }

    @Test
    public void remove_cityinfo_works_as_expected() {

        // given
        CityInfo cityInfo = new CityInfo("origin-name", "origin-country");

        when(cityInfoToRouteInfos.opsForValue())
                .thenReturn(cityInfoToRouteInfosValueOps);

        when(cityInfoToRouteInfosValueOps.getOperations())
                .thenReturn(cityInfoRouteInfosRedisOperations);

        when(cityInfoRouteInfosRedisOperations.delete(cityInfo))
                .thenReturn(true);

        // when - then
        StepVerifier.create(cacheService.remove(cityInfo))
                .expectNext(true)
                .verifyComplete();

        verify(cityInfoToRouteInfos, times(1)).opsForValue();
        verify(cityInfoToRouteInfosValueOps, times(1)).getOperations();
        verify(cityInfoRouteInfosRedisOperations, times(1)).delete(cityInfo);
    }

    @Test
    public void remove_routeid_works_as_expected() {

        // given
        String routeId = UUID.randomUUID().toString();

        when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);

        when(routeIdToRouteInfoValueOps.getOperations())
                .thenReturn(routeIdToRouteInfoRedisOperations);

        when(routeIdToRouteInfoRedisOperations.delete(routeId))
                .thenReturn(true);

        // when - then
        StepVerifier.create(cacheService.remove(routeId))
                .expectNext(true)
                .verifyComplete();

        verify(routeIdToRouteInfo, times(1)).opsForValue();
        verify(routeIdToRouteInfoValueOps, times(1)).getOperations();
        verify(routeIdToRouteInfoRedisOperations, times(1)).delete(routeId);
    }

    @Test
    public void upsert_cityinfo_routeinfo_works_as_expected() {

        // given
        CityInfo cityInfo = new CityInfo("origin-name", "origin-country");

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo routeInfo = new RouteInfo(null, originCityInfo, destinyCityInfo, departureTime, arrivalTime);

        when(cityInfoToRouteInfos.opsForValue())
                .thenReturn(cityInfoToRouteInfosValueOps);

        List<RouteInfo> routeInfos = Collections.singletonList(routeInfo);

        // when - then
        Pair<CityInfo, List<RouteInfo>> result = Pair.with(cityInfo, routeInfos);

        StepVerifier.create(cacheService.upsert(cityInfo, routeInfo))
                .expectNext(result)
                .verifyComplete();

        verify(cityInfoToRouteInfosValueOps).set(cityInfo, routeInfos);
    }

    @Test
    public void upsert_routeid_routeinfo_works_as_expected() {

        // given
        String routeId = UUID.randomUUID().toString();

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo routeInfo = new RouteInfo(null, originCityInfo, destinyCityInfo, departureTime, arrivalTime);

        when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);


        // when - then
        Pair<String, RouteInfo> result = Pair.with(routeId, routeInfo);

        StepVerifier.create(cacheService.upsert(routeId, routeInfo))
                .expectNext(result)
                .verifyComplete();

        verify(routeIdToRouteInfoValueOps).set(routeId, routeInfo);
    }
}