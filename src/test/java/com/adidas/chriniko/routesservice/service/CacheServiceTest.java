package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.codahale.metrics.Meter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceTest {

    @Mock
    private RedisTemplate<CityInfo, RouteInfo> cityInfoToRouteInfo;
    @Mock
    private RedisTemplate<String, RouteInfo> routeIdToRouteInfo;
    @Mock
    private ValueOperations<String, RouteInfo> routeIdToRouteInfoValueOps;

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
        cacheService = new CacheService(cityInfoToRouteInfo,
                routeIdToRouteInfo,
                cacheHitFindByCityInfo,
                cacheMissFindByCityInfo,
                cacheHitFindByRouteId,
                cacheMissFindByRouteId
        );
    }


    @Test
    public void get_cityinfo_works_as_expected_cache_hit_case() {

        // given
        String id = UUID.randomUUID().toString();
        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo result = new RouteInfo(id, originCityInfo, destinyCityInfo, departureTime, arrivalTime);

        String routeId = "xyz";
        Mockito.when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);

        Mockito.when(routeIdToRouteInfoValueOps.get(routeId))
                .thenReturn(result);

        // when - then
        StepVerifier
                .create(cacheService.get(routeId))
                .expectNext(result)
                .verifyComplete();

        Mockito.verify(cacheHitFindByRouteId, Mockito.times(1)).mark();
        Mockito.verify(cacheMissFindByRouteId, Mockito.times(0)).mark();

    }

    @Test
    public void get_cityinfo_works_as_expected_cache_miss_case() {

        // given
        String routeId = "xyz";
        Mockito.when(routeIdToRouteInfo.opsForValue())
                .thenReturn(routeIdToRouteInfoValueOps);

        Mockito.when(routeIdToRouteInfoValueOps.get(routeId))
                .thenReturn(null);

        // when - then
        StepVerifier
                .create(cacheService.get(routeId))
                .verifyComplete();

        Mockito.verify(cacheHitFindByRouteId, Mockito.times(0)).mark();
        Mockito.verify(cacheMissFindByRouteId, Mockito.times(1)).mark();

    }

    @Test
    public void get_routeid_works_as_expected() {

        //TODO
        assertEquals(1, 1);

    }

    @Test
    public void remove_cityinfo_works_as_expected() {

        //TODO
        assertEquals(1, 1);

    }

    @Test
    public void remove_routeid_works_as_expected() {

        //TODO
        assertEquals(1, 1);

    }

    @Test
    public void upsert_cityinfo_routeinfo_works_as_expected() {

        //TODO
        assertEquals(1, 1);

    }

    @Test
    public void upsert_routeid_routeinfo_works_as_expected() {

        //TODO
        assertEquals(1, 1);

    }
}