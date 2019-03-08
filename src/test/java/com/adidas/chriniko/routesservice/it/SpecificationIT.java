package com.adidas.chriniko.routesservice.it;


import com.adidas.chriniko.routesservice.Chrono;
import com.adidas.chriniko.routesservice.RoutesServiceApplication;
import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfoResult;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = RoutesServiceApplication.class,
        properties = {"application.properties"}
)

@RunWith(SpringRunner.class)
public class SpecificationIT {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RedisTemplate<CityInfo, List<RouteInfo>> cityInfoToRouteInfosCache;

    @Autowired
    private RedisTemplate<String, RouteInfo> routeIdToRouteInfoCache;

    @Before
    public void before() {
        webClient = WebClient.create("http://localhost:" + port + "/api/route-info");
    }

    @Test
    public void complete_flow_works_as_expected() {

        // given
        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        RouteInfo routeInfo = new RouteInfo(null, originCityInfo, destinyCityInfo, departureTime, arrivalTime);


        // when - create a new route info
        final RouteInfo resultFromCreateNewRouteInfo = webClient.post()
                .body(BodyInserters.fromObject(routeInfo))
                .exchange()
                .block()
                .body(BodyExtractors.toMono(RouteInfo.class))
                .block();


        // then
        assertNotNull(resultFromCreateNewRouteInfo);
        assertEquals("origin city", resultFromCreateNewRouteInfo.getCity().getName());
        assertEquals("origin country", resultFromCreateNewRouteInfo.getCity().getCountry());
        assertEquals("destiny city", resultFromCreateNewRouteInfo.getDestinyCity().getName());
        assertEquals("destiny country", resultFromCreateNewRouteInfo.getDestinyCity().getCountry());

        assertEquals(Chrono.map(departureTime), Chrono.map(resultFromCreateNewRouteInfo.getDepartureTime()));
        assertEquals(Chrono.map(arrivalTime), Chrono.map(resultFromCreateNewRouteInfo.getArrivalTime()));

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertNotNull(routeRepository.find(resultFromCreateNewRouteInfo.getId()));
                    assertNull(cityInfoToRouteInfosCache.opsForValue().get(originCityInfo));
                });


        // when - find by city info
        final RouteInfoResult resultFindByCityInfoResult = webClient.post()
                .uri("/search")
                .body(BodyInserters.fromObject(originCityInfo))
                .exchange()
                .block()
                .body(BodyExtractors.toMono(RouteInfoResult.class))
                .block();


        // then
        RouteInfo resultFindByCityInfo = resultFindByCityInfoResult.getResults().get(0);

        assertNotNull(resultFindByCityInfo);
        assertEquals("origin city", resultFindByCityInfo.getCity().getName());
        assertEquals("origin country", resultFindByCityInfo.getCity().getCountry());
        assertEquals("destiny city", resultFindByCityInfo.getDestinyCity().getName());
        assertEquals("destiny country", resultFindByCityInfo.getDestinyCity().getCountry());

        assertEquals(Chrono.map(departureTime), Chrono.map(resultFindByCityInfo.getDepartureTime()));
        assertEquals(Chrono.map(arrivalTime), Chrono.map(resultFindByCityInfo.getArrivalTime()));


        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertNotNull(cityInfoToRouteInfosCache.opsForValue().get(originCityInfo));
                });


        // when - find by route id
        String routeId = resultFindByCityInfo.getId();

        assertNull(routeIdToRouteInfoCache.opsForValue().get(routeId));

        final RouteInfo resultFindByRouteId = webClient.get()
                .uri("/" + routeId)
                .exchange()
                .block()
                .body(BodyExtractors.toMono(RouteInfo.class))
                .block();


        // then
        assertNotNull(resultFindByRouteId);
        assertEquals("origin city", resultFindByRouteId.getCity().getName());
        assertEquals("origin country", resultFindByRouteId.getCity().getCountry());
        assertEquals("destiny city", resultFindByRouteId.getDestinyCity().getName());
        assertEquals("destiny country", resultFindByRouteId.getDestinyCity().getCountry());

        assertEquals(Chrono.map(departureTime), Chrono.map(resultFindByRouteId.getDepartureTime()));
        assertEquals(Chrono.map(arrivalTime), Chrono.map(resultFindByRouteId.getArrivalTime()));

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertNotNull(routeIdToRouteInfoCache.opsForValue().get(routeId));
                });


        // when - update a route info record
        String id = resultFindByRouteId.getId();

        resultFindByRouteId.getCity().setName(resultFindByRouteId.getCity().getName() + " [UPDATED]");
        resultFindByRouteId.getCity().setCountry(resultFindByRouteId.getCity().getCountry() + " [UPDATED]");

        resultFindByRouteId.getDestinyCity().setName(resultFindByRouteId.getDestinyCity().getName() + " [UPDATED]");
        resultFindByRouteId.getDestinyCity().setCountry(resultFindByRouteId.getDestinyCity().getCountry() + " [UPDATED]");

        resultFindByCityInfo.setDepartureTime(
                resultFindByCityInfo
                        .getDepartureTime()
                        .plusSeconds(TimeUnit.SECONDS.convert(1, TimeUnit.HOURS))
        );

        resultFindByCityInfo.setArrivalTime(
                resultFindByCityInfo
                        .getArrivalTime()
                        .plusSeconds(TimeUnit.SECONDS.convert(1, TimeUnit.HOURS))
        );

        int cityInfoKeys = cityInfoToRouteInfosCache.opsForValue().getOperations().keys(new CityInfo("*", "*")).size();
        int routeIdKeys = routeIdToRouteInfoCache.opsForValue().getOperations().keys("*").size();

        final RouteInfo resultUpdate = webClient.put()
                .uri("/" + id)
                .body(BodyInserters.fromObject(resultFindByRouteId))
                .exchange()
                .block()
                .body(BodyExtractors.toMono(RouteInfo.class))
                .block();


        // then
        assertNotNull(resultUpdate);

        assertEquals(resultFindByRouteId.getCity().getName(), resultUpdate.getCity().getName());
        assertEquals(resultFindByRouteId.getCity().getCountry(), resultUpdate.getCity().getCountry());
        assertEquals(resultFindByRouteId.getDestinyCity().getName(), resultUpdate.getDestinyCity().getName());
        assertEquals(resultFindByRouteId.getDestinyCity().getCountry(), resultUpdate.getDestinyCity().getCountry());

        assertEquals(Chrono.map(resultFindByRouteId.getDepartureTime()), Chrono.map(resultUpdate.getDepartureTime()));
        assertEquals(Chrono.map(resultFindByRouteId.getArrivalTime()), Chrono.map(resultUpdate.getArrivalTime()));


        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    CityInfo cityInfo = new CityInfo(
                            resultUpdate.getCity().getName(),
                            resultUpdate.getCity().getCountry());

                    List<RouteInfo> cityInfoToRouteInfosCacheResult = cityInfoToRouteInfosCache.opsForValue().get(cityInfo);
                    assertTrue(cityInfoToRouteInfosCacheResult.contains(resultUpdate));

                    Set<CityInfo> k1 = cityInfoToRouteInfosCache.opsForValue().getOperations().keys(new CityInfo("*", "*"));
                    assertEquals(cityInfoKeys, k1.size());

                    RouteInfo routeIdToRouteInfoCacheResult = routeIdToRouteInfoCache.opsForValue().get(resultUpdate.getId());
                    assertEquals(resultUpdate, routeIdToRouteInfoCacheResult);

                    Set<String> k2 = routeIdToRouteInfoCache.opsForValue().getOperations().keys("*");
                    assertEquals(routeIdKeys, k2.size());

                });


        // when - delete a route info record by id
        final RouteInfo resultDelete = webClient.delete()
                .uri("/" + resultUpdate.getId())
                .exchange()
                .block()
                .body(BodyExtractors.toMono(RouteInfo.class))
                .block();


        // then
        assertNotNull(resultDelete);

        assertEquals(resultUpdate.getCity().getName(), resultDelete.getCity().getName());
        assertEquals(resultUpdate.getCity().getCountry(), resultDelete.getCity().getCountry());
        assertEquals(resultUpdate.getDestinyCity().getName(), resultDelete.getDestinyCity().getName());
        assertEquals(resultUpdate.getDestinyCity().getCountry(), resultDelete.getDestinyCity().getCountry());

        assertEquals(Chrono.map(resultUpdate.getDepartureTime()), Chrono.map(resultDelete.getDepartureTime()));
        assertEquals(Chrono.map(resultUpdate.getArrivalTime()), Chrono.map(resultDelete.getArrivalTime()));

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertEquals(Optional.empty(), routeRepository.find(resultDelete.getId()));

                    assertNull(routeIdToRouteInfoCache.opsForValue().get(resultDelete.getId()));

                    assertNull(cityInfoToRouteInfosCache.opsForValue().get(resultDelete.getCity()));

                });

    }
}
