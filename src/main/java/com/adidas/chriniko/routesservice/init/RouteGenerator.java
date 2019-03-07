package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.error.ProcessingException;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import lombok.extern.log4j.Log4j2;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.paukov.combinatorics.CombinatoricsFactory.createPermutationWithRepetitionGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.createVector;

@Log4j2

@Component
public class RouteGenerator {

    private static final int BATCH_SIZE = 25;

    private final RouteRepository routeRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RouteGenerator(RouteRepository routeRepository, TransactionTemplate transactionTemplate) {
        this.routeRepository = routeRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void generate(Map<String, List<String>> citiesByCountry) {

        // Note: display data info size.
        int totalCountries = citiesByCountry.size();

        IntSummaryStatistics summaryStatisticsCitiesSize = citiesByCountry
                .values()
                .stream()
                .mapToInt(List::size)
                .summaryStatistics();

        long averageCitiesPerCountry = (long) Math.ceil(summaryStatisticsCitiesSize.getAverage());

        /*
            //TODO validate if is correct type...
            Number of permutations (order matters) of n things taken r at a time:
            P(n,r) = n! / (n-r)!
         */
        int p = 2; // (origin, destiny)
        Long numberOfPerms = factorial(averageCitiesPerCountry).divide(factorial(averageCitiesPerCountry - p)).longValue();

        long estimatedNumberOfRecordsInDbAfterExecution = totalCountries * numberOfPerms;

        log.debug("totalCountries: {}, averageCitiesPerCountry: {}, estimatedNumberOfRecordsInDbAfterExecution: {}",
                totalCountries, averageCitiesPerCountry, estimatedNumberOfRecordsInDbAfterExecution);


        // Note: calculate data.
        final Random random = new Random();

        final Map<String, List<RouteEntity>> routesInfoByCountry = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> citiesByCountryRecord : citiesByCountry.entrySet()) {

            String country = citiesByCountryRecord.getKey();
            List<String> cities = citiesByCountryRecord.getValue();

            ICombinatoricsVector<String> vector = createVector(cities);

            Generator<String> generator = createPermutationWithRepetitionGenerator(vector, 2 /* 2 == (origin, destiny) */);
            List<ICombinatoricsVector<String>> generatedData = generator.generateAllObjects();

            final List<RouteEntity> routes = new LinkedList<>();

            for (ICombinatoricsVector<String> permutation : generatedData) {
                String originCity = permutation.getValue(0);
                String destinyCity = permutation.getValue(1);

                Instant departureTime = Instant.now();

                int hours = random.nextInt(12) + 1;

                Instant arrivalTime = departureTime.plusSeconds(TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS));

                RouteEntity routeEntity = new RouteEntity(
                        originCity,
                        country,
                        destinyCity,
                        country,
                        departureTime,
                        arrivalTime
                );

                routes.add(routeEntity);
            }
            routesInfoByCountry.put(country, routes);
            log.debug("just stored: {} ---> {}", country, generatedData);
        }

        // Note: save data.
        int accurateNumberOfRecordsInDbAfterExecution = routesInfoByCountry.values().stream().mapToInt(Collection::size).sum();
        log.debug("accurateNumberOfRecordsInDbAfterExecution: {}", accurateNumberOfRecordsInDbAfterExecution);

        int workers = routesInfoByCountry.size();
        ExecutorService workersPool = Executors.newFixedThreadPool(routesInfoByCountry.size(), new ThreadFactory() {
            private final AtomicInteger id = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setName("route-generator-worker-" + id.incrementAndGet());
                return t;
            }
        });

        final CountDownLatch countDownLatch = new CountDownLatch(workers);

        for (Map.Entry<String, List<RouteEntity>> routeInfoRecord : routesInfoByCountry.entrySet()) {

            workersPool.submit(() -> {

                List<RouteEntity> routes = routeInfoRecord.getValue();
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

    //TODO optimize batch insert...
    private void store(List<RouteEntity> batchSave) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                batchSave.forEach(routeRepository::insert);
            }
        });
    }

    private static BigInteger factorial(long number) {
        return factorial(BigInteger.valueOf(number));
    }

    private static BigInteger factorial(BigInteger number) {
        BigInteger result = BigInteger.valueOf(1);

        for (long factor = 2; factor <= number.longValue(); factor++) {
            result = result.multiply(BigInteger.valueOf(factor));
        }

        return result;
    }

}
