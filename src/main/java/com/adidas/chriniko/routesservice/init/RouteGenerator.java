package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.error.ProcessingException;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2

@Component
public class RouteGenerator {

    private final RouteRepository routeRepository;
    private final TransactionTemplate transactionTemplate;
    private final RouteDataGenerator routeDataGenerator;

    @Autowired
    public RouteGenerator(RouteRepository routeRepository,
                          TransactionTemplate transactionTemplate,
                          RouteDataGenerator routeDataGenerator) {
        this.routeRepository = routeRepository;
        this.transactionTemplate = transactionTemplate;
        this.routeDataGenerator = routeDataGenerator;
    }

    public void generate(Map<String, List<String>> citiesByCountry) {
        displayDataSizeInfo(citiesByCountry);

        RouteDataGenerator.RouteDataGeneratorResult dataGeneratorResult = routeDataGenerator.invoke(citiesByCountry);

        Map<String, List<List<RouteEntity>>> itinerariesInfoByCountry = dataGeneratorResult.getItinerariesInfoByCountry();
        int countriesWithOnlyOneCity = dataGeneratorResult.getCountriesWithOnlyOneCity();

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

        int noOfWorkers = itinerariesInfoByCountry.size();
        log.debug("total db workers: {}", noOfWorkers);

        ExecutorService routeStoreWorkers = Executors.newFixedThreadPool(itinerariesInfoByCountry.size(), new ThreadFactory() {
            private final AtomicInteger id = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setName("route-store-worker-" + id.incrementAndGet());
                return t;
            }
        });

        final CountDownLatch rendezvous = new CountDownLatch(noOfWorkers);

        for (Map.Entry<String, List<List<RouteEntity>>> itineraryInfoRecord : itinerariesInfoByCountry.entrySet()) {

            routeStoreWorkers.submit(() -> {

                List<List<RouteEntity>> itineraries = itineraryInfoRecord.getValue();
                List<RouteEntity> routes = itineraries.stream().flatMap(Collection::stream).collect(Collectors.toList());
                store(routes);

                rendezvous.countDown();
            });
        }

        try {
            rendezvous.await();
        } catch (InterruptedException error) {
            throw new ProcessingException(error);
        }
        routeStoreWorkers.shutdown();
    }

    private void store(List<RouteEntity> routes) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                routeRepository.batchInsert(routes);
            }
        });
    }
}
