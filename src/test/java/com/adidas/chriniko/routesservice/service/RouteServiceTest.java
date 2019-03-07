package com.adidas.chriniko.routesservice.service;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private CacheService cacheService;

    private RouteService routeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        routeService = new RouteService(routeRepository, transactionTemplate, cacheService);
    }

    @Test
    public void find_by_cityinfo_cache_miss_case() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();

        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(cacheService.get(originCityInfo))
                .thenReturn(Mono.empty());

        Mockito.when(routeRepository.find(originCityInfo.getName(), originCityInfo.getCountry()))
                .thenReturn(Optional.of(routeEntity));

        Mockito.when(cacheService.upsert(originCityInfo, routeInfo))
                .thenReturn(Mono.just(Pair.with(originCityInfo, routeInfo)));

        // when - then
        StepVerifier
                .create(routeService.find(originCityInfo))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verify(cacheService).upsert(originCityInfo, routeInfo);
    }

    @Test
    public void find_by_cityinfo_cache_no_result_case() {

        // given

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");


        Mockito.when(cacheService.get(originCityInfo))
                .thenReturn(Mono.empty());

        Mockito.when(routeRepository.find(originCityInfo.getName(), originCityInfo.getCountry()))
                .thenReturn(Optional.empty());


        // when - then
        StepVerifier
                .create(routeService.find(originCityInfo))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void find_by_cityinfo_cache_hit_case() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();
        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        Mockito.when(cacheService.get(originCityInfo))
                .thenReturn(Mono.just(routeInfo));

        // when - then
        StepVerifier
                .create(routeService.find(originCityInfo))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verifyZeroInteractions(routeRepository);
    }

    @Test
    public void create() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();
        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(transactionTemplate.execute(Mockito.any(TransactionCallbackWithoutResult.class)))
                .then(invocationOnMock -> {
                    routeRepository.insert(routeEntity);
                    return null;
                });

        // when - then
        StepVerifier
                .create(routeService.create(routeInfo))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verify(transactionTemplate).execute(Mockito.any(TransactionCallbackWithoutResult.class));
        Mockito.verify(routeRepository).insert(Mockito.any(RouteEntity.class));
    }

    @Test
    public void update_record_not_exists_case() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();
        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        Mockito.when(routeRepository.find(id))
                .thenReturn(Optional.empty());


        // when - then
        StepVerifier.create(routeService.update(id, routeInfo))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    public void update_record_exists_case() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();
        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(routeRepository.find(id))
                .thenReturn(Optional.of(routeEntity));


        Mockito.when(cacheService.remove(id))
                .thenReturn(Mono.just(true));

        Mockito.when(cacheService.remove(originCityInfo))
                .thenReturn(Mono.just(true));

        Mockito.when(cacheService.upsert(id, routeInfo))
                .thenReturn(Mono.just(Pair.with(id, routeInfo)));

        Mockito.when(cacheService.upsert(originCityInfo, routeInfo))
                .thenReturn(Mono.just(Pair.with(originCityInfo, routeInfo)));

        // when - then
        StepVerifier.create(routeService.update(id, routeInfo))
                .expectNext(routeInfo)
                .verifyComplete();

    }

    @Test
    public void find_by_routeid_miss_case() {

        // given
        String routeId = UUID.randomUUID().toString();

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();

        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(cacheService.get(routeId))
                .thenReturn(Mono.empty());

        Mockito.when(routeRepository.find(routeId))
                .thenReturn(Optional.of(routeEntity));

        Mockito.when(cacheService.upsert(routeId, routeInfo))
                .thenReturn(Mono.just(Pair.with(routeId, routeInfo)));

        // when - then
        StepVerifier.create(routeService.find(routeId))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verify(cacheService).upsert(routeId, routeInfo);

    }

    @Test
    public void find_by_routeid_hit_case() {

        // given
        String routeId = UUID.randomUUID().toString();

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();

        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        Mockito.when(cacheService.get(routeId))
                .thenReturn(Mono.just(routeInfo));

        // when - then
        StepVerifier.create(routeService.find(routeId))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verify(cacheService, Mockito.times(0))
                .upsert(routeId, routeInfo);
    }

    @Test
    public void find_by_routeid_no_result_case() {

        // given
        String routeId = UUID.randomUUID().toString();

        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();

        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(cacheService.get(routeId))
                .thenReturn(Mono.empty());

        Mockito.when(routeRepository.find(routeId))
                .thenReturn(Optional.empty());

        // when - then
        StepVerifier.create(routeService.find(routeId))
                .expectError(ResponseStatusException.class)
                .verify();

        Mockito.verify(cacheService, Mockito.times(0)).upsert(routeId, routeInfo);
    }

    @Test
    public void delete_record_not_exists() {

        // given
        String id = UUID.randomUUID().toString();

        Mockito.when(routeRepository.find(id))
                .thenReturn(Optional.empty());

        // when - then
        StepVerifier
                .create(routeService.delete(id))
                .expectError(ResponseStatusException.class)
                .log()
                .verify();

    }

    @Test
    public void delete_record_exists() {

        // given
        Instant departureTime = Instant.now();
        long timeToArrive = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
        Instant arrivalTime = departureTime.plusSeconds(timeToArrive);

        CityInfo originCityInfo = new CityInfo("origin city", "origin country");
        CityInfo destinyCityInfo = new CityInfo("destiny city", "destiny country");

        String id = UUID.randomUUID().toString();
        RouteInfo routeInfo = new RouteInfo(
                id,
                originCityInfo,
                destinyCityInfo,
                departureTime,
                arrivalTime);

        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setId(id);
        routeEntity.setOriginCityName(originCityInfo.getName());
        routeEntity.setOriginCountry(originCityInfo.getCountry());
        routeEntity.setDestinyCityName(destinyCityInfo.getName());
        routeEntity.setDestinyCountry(destinyCityInfo.getCountry());
        routeEntity.setDepartureTime(departureTime);
        routeEntity.setArrivalTime(arrivalTime);

        Mockito.when(routeRepository.find(id))
                .thenReturn(Optional.of(routeEntity));

        Mockito.when(cacheService.remove(originCityInfo))
                .thenReturn(Mono.just(true));

        Mockito.when(cacheService.remove(routeEntity.getId()))
                .thenReturn(Mono.just(true));

        Mockito.when(transactionTemplate.execute(Mockito.any(TransactionCallbackWithoutResult.class)))
                .then(invocationOnMock -> {
                    routeRepository.delete(routeEntity);
                    return null;
                });

        // when - then
        StepVerifier
                .create(routeService.delete(id))
                .expectNext(routeInfo)
                .verifyComplete();

        Mockito.verify(transactionTemplate).execute(Mockito.any(TransactionCallbackWithoutResult.class));
        Mockito.verify(routeRepository).delete(Mockito.any(RouteEntity.class));
        Mockito.verify(cacheService).remove(Mockito.any(CityInfo.class));
        Mockito.verify(cacheService).remove(Mockito.anyString());
    }
}