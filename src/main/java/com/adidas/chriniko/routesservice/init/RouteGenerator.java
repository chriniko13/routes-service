package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2

@Component
public class RouteGenerator {

    private static final boolean DISPLAY_STORING_INFO = false;

    private static final int NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY = 3;


    private final RouteRepository routeRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RouteGenerator(RouteRepository routeRepository, TransactionTemplate transactionTemplate) {
        this.routeRepository = routeRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void generate(Map<String, List<String>> citiesByCountry) {
        displayDataSizeInfo(citiesByCountry);

        // Note: calculate data.
        final SecureRandom random = new SecureRandom();

        final Map<String, List<List<RouteEntity>>> itinerariesInfoByCountry = new LinkedHashMap<>();

        int countriesWithOnlyOneCity = 0;

        for (Map.Entry<String, List<String>> citiesByCountryRecord : citiesByCountry.entrySet()) {

            String country = citiesByCountryRecord.getKey();
            List<String> cities = citiesByCountryRecord.getValue();

            // We need at least two in order to create a route (a,b) for itinerary size of 1.
            if (cities.size() == 1) {
                countriesWithOnlyOneCity++;
                continue;
            }

            // first pick a random root city
            int rootCityIdx = random.nextInt(cities.size());
            String rootCity = cities.get(rootCityIdx);

            List<List<RouteEntity>> itineraries = new ArrayList<>(NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY);

            for (int k = 1; k <= NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY; k++) {

                // calculate size of itinerary, eg: a --> b --> c--> ... (size == 3)
                int maxItinerarySize = cities.size() - 1; /* [city a, city b, city c] === [(a,b), (b,c)] === 2 records in db */
                int itinerarySize = random.nextInt(maxItinerarySize) + 1;

                // calculate next city idx and do the 'binding'
                Set<Integer> alreadyPickedIdxs = new LinkedHashSet<>();
                alreadyPickedIdxs.add(rootCityIdx);

                String previousCity = rootCity;

                List<RouteEntity> routes = new ArrayList<>(itinerarySize);

                for (int i = 1; i <= itinerarySize; i++) {
                    int nextCityIdx;
                    do {
                        nextCityIdx = random.nextInt(cities.size());
                    } while (alreadyPickedIdxs.contains(nextCityIdx));

                    alreadyPickedIdxs.add(nextCityIdx);

                    String nextCity = cities.get(nextCityIdx);

                    Instant departureTime = Instant.now();
                    Instant arrivalTime = departureTime.plusSeconds(TimeUnit.SECONDS.convert(random.nextInt(4) + 1, TimeUnit.HOURS));

                    RouteEntity route = new RouteEntity(
                            previousCity,
                            country,
                            nextCity,
                            country,
                            departureTime,
                            arrivalTime
                    );

                    routes.add(route);
                    previousCity = nextCity;
                }

                itineraries.add(routes);

                log(country, routes);
            }

            itinerariesInfoByCountry.put(country, itineraries);
        }

        saveData(itinerariesInfoByCountry, countriesWithOnlyOneCity);
    }

    private void displayDataSizeInfo(Map<String, List<String>> citiesByCountry) {
        int totalCountries = citiesByCountry.size();

        IntSummaryStatistics summaryStatisticsCitiesSize = citiesByCountry
                .values()
                .stream()
                .mapToInt(List::size)
                .summaryStatistics();

        long averageCitiesPerCountry = (long) Math.ceil(summaryStatisticsCitiesSize.getAverage());
        int maxCitiesPerCountry = summaryStatisticsCitiesSize.getMax();
        int minCitiesPerCountry = summaryStatisticsCitiesSize.getMin();
        long sumOfCities = summaryStatisticsCitiesSize.getSum();

        log.debug("totalCountries: {}, averageCitiesPerCountry: {}, maxCitiesPerCountry: {}, minCitiesPerCountry: {}, sumOfCities: {}",
                totalCountries, averageCitiesPerCountry, maxCitiesPerCountry, minCitiesPerCountry, sumOfCities);
    }

    private void saveData(Map<String, List<List<RouteEntity>>> itinerariesInfoByCountry, int countriesWithOnlyOneCity) {
        log.debug("countries with only one city: {}", countriesWithOnlyOneCity);

        int accurateNumberOfRecordsInDbAfterExecution = itinerariesInfoByCountry
                .values()
                .stream()
                .flatMap(Collection::stream)
                .mapToInt(Collection::size).sum();

        log.debug("accurateNumberOfRecordsInDbAfterExecution: {}", accurateNumberOfRecordsInDbAfterExecution);

        int workers = itinerariesInfoByCountry.size();
        log.debug("total db workers: {}", workers);

        ExecutorService workersPool = Executors.newFixedThreadPool(itinerariesInfoByCountry.size(), new ThreadFactory() {
            private final AtomicInteger id = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setName("route-generator-worker-" + id.incrementAndGet());
                return t;
            }
        });

        final CountDownLatch countDownLatch = new CountDownLatch(workers);

        for (Map.Entry<String, List<List<RouteEntity>>> itineraryInfoRecord : itinerariesInfoByCountry.entrySet()) {

            workersPool.submit(() -> {

                List<List<RouteEntity>> itineraries = itineraryInfoRecord.getValue();
                List<RouteEntity> routes = itineraries.stream().flatMap(Collection::stream).collect(Collectors.toList());
                store(routes);

                countDownLatch.countDown();
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        workersPool.shutdown();
    }

    private void store(List<RouteEntity> routes) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                routeRepository.batchInsert(routes);
            }
        });
    }

    private void log(String country, List<RouteEntity> routes) {
        if (DISPLAY_STORING_INFO) {
            List<Pair<String, String>> routesForPrinting = routes
                    .stream()
                    .map(r -> Pair.with(r.getOriginCityName(), r.getDestinyCityName()))
                    .collect(Collectors.toList());

            log.debug("just stored: {} ---> {}", country, routesForPrinting);
        }
    }

}
