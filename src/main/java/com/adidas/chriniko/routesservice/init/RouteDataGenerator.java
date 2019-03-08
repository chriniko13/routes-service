package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.error.ProcessingException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2

@Component
public class RouteDataGenerator {

    private static final int NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY = 4;

    @Value("${route-data-generator.display-storing-info}")
    private boolean displayStoringInfo;

    @Getter
    public static class RouteDataGeneratorResult {
        private Map<String, List<List<RouteEntity>>> itinerariesInfoByCountry = new LinkedHashMap<>();
        private int countriesWithOnlyOneCity = 0;
    }

    public RouteDataGeneratorResult invoke(Map<String, List<String>> citiesByCountry) {

        RouteDataGeneratorResult result = new RouteDataGeneratorResult();

        ExecutorService routeGeneratorWorkers = Executors.newFixedThreadPool(NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY, new ThreadFactory() {
            private final AtomicInteger id = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setName("route-generator-worker-" + id.incrementAndGet());
                return t;
            }
        });

        final SecureRandom random = new SecureRandom();

        for (Map.Entry<String, List<String>> citiesByCountryRecord : citiesByCountry.entrySet()) {

            String country = citiesByCountryRecord.getKey();
            List<String> cities = citiesByCountryRecord.getValue();

            // We need at least two in order to create a route (a,b) for itinerary size of 1.
            if (cities.size() == 1) {
                result.countriesWithOnlyOneCity++;
                continue;
            }

            // first pick a random root city
            final int rootCityIdx = random.nextInt(cities.size());
            final String rootCity = cities.get(rootCityIdx);

            final List<List<RouteEntity>> itineraries = Collections.synchronizedList(new ArrayList<>(NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY));

            final CountDownLatch rendezvous = new CountDownLatch(NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY);

            for (int k = 1; k <= NO_OF_ITINERARIES_FOR_SELECTED_ROOT_CITY; k++) {

                routeGeneratorWorkers.submit(() -> {

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

                        //TODO keep previous time in order to create more realistic times...
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

                    rendezvous.countDown();
                });
            }

            try {
                rendezvous.await();
            } catch (InterruptedException e) {
                throw new ProcessingException(e);
            }

            result.itinerariesInfoByCountry.put(country, itineraries);
        }

        routeGeneratorWorkers.shutdown();

        return result;
    }

    private void log(String country, List<RouteEntity> routes) {
        if (displayStoringInfo) {
            List<Pair<String, String>> routesForPrinting = routes
                    .stream()
                    .map(r -> Pair.with(r.getOriginCityName(), r.getDestinyCityName()))
                    .collect(Collectors.toList());

            log.debug("just stored: {} ---> {}", country, routesForPrinting);
        }
    }


}
