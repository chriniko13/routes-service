package com.adidas.chriniko.routesservice.configuration;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import com.adidas.chriniko.routesservice.repository.RouteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfiguration {

    // Note: add beans depending on your needs.

    @Bean
    @Order(0)
    public CommandLineRunner populateDb(RouteRepository routeRepository,
                                        TransactionTemplate transactionTemplate) {
        return args -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                    routeRepository.deleteAll();

                    for (int i = 1; i <= 100; i++) {
                        RouteEntity routeEntity = new RouteEntity();

                        routeEntity.setOriginCityName("origin " + i);
                        routeEntity.setOriginCountry("origin country " + i);

                        routeEntity.setDestinyCityName("destiny " + i);
                        routeEntity.setDestinyCountry("destiny country " + i);


                        Instant departureTime = Instant.now();

                        long seconds = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);

                        routeEntity.setDepartureTime(departureTime);
                        routeEntity.setArrivalTime(departureTime.plusSeconds(seconds));

                        routeRepository.insert(routeEntity);
                    }
                }
            });
        };
    }

}
