package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2

@Component
public class RouteDataGenerator {


    @Value("${route-data-generator.no-of-itineraries-for-selected-root-city}")
    private int noOfItinerariesForSelectedRootCity;

    @Value("${route-data-generator.display-storing-info}")
    private boolean displayStoringInfo;

    @Getter
    public static class RouteDataGeneratorResult {
        private Map<String, List<List<RouteEntity>>> itinerariesInfoByCountry = new LinkedHashMap<>();
        private int countriesWhichNotSatisfyNoOfItinerariesForSelectedRootCity = 0;
    }

    public RouteDataGeneratorResult invoke(Map<String, List<String>> citiesByCountry) {

        final RouteDataGeneratorResult result = new RouteDataGeneratorResult();

        final SecureRandom random = new SecureRandom();

        for (Map.Entry<String, List<String>> citiesByCountryRecord : citiesByCountry.entrySet()) {

            String country = citiesByCountryRecord.getKey();
            List<String> cities = citiesByCountryRecord.getValue();

            if (cities.size() <= noOfItinerariesForSelectedRootCity) {
                result.countriesWhichNotSatisfyNoOfItinerariesForSelectedRootCity++;
                continue;
            }

            // first pick a random root city
            final int rootCityIdx = random.nextInt(cities.size());
            final String rootCity = cities.get(rootCityIdx);
            cities.remove(rootCity);

            log.trace("noOfItinerariesForSelectedRootCity: {} --- cities.size(): {}", noOfItinerariesForSelectedRootCity, cities.size());
            if (noOfItinerariesForSelectedRootCity > cities.size()) {
                throw new IllegalStateException("not enough cities to apply the request number of itineraries for selected root city (partition could not be applied), "
                        + "noOfItinerariesForSelectedRootCity="
                        + noOfItinerariesForSelectedRootCity
                        + ", cities.size()="
                        + cities.size());
            }

            List<List<String>> partitionedCities = Lists.partition(cities, cities.size() / noOfItinerariesForSelectedRootCity);

            final List<List<RouteEntity>> itineraries = new ArrayList<>(noOfItinerariesForSelectedRootCity);

            for (int k = 1; k <= noOfItinerariesForSelectedRootCity; k++) {

                List<String> citiesToChooseFrom = partitionedCities.get(k - 1);

                // calculate size of itinerary, eg: a --> b --> c--> ... (size == 3)
                int maxItinerarySize = citiesToChooseFrom.size(); /* [city a, city b, city c] === [(a,b), (b,c)] === 2 records in db */
                int itinerarySize = random.nextInt(maxItinerarySize);

                // calculate next city idx and do the 'binding'
                String previousCity = rootCity;
                Instant previousArrivalTime = Instant.now();

                List<RouteEntity> routes = new ArrayList<>(itinerarySize);

                for (int i = 1; i <= itinerarySize; i++) {

                    String nextCity = citiesToChooseFrom.get(i - 1);

                    Instant departureTime = previousArrivalTime;
                    Instant arrivalTime = departureTime.plusSeconds(TimeUnit.SECONDS.convert(random.nextInt(4) + 1, TimeUnit.HOURS));

                    previousArrivalTime = arrivalTime;

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

            result.itinerariesInfoByCountry.put(country, itineraries);
        }


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
