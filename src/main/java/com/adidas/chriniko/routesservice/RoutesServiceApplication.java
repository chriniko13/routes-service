package com.adidas.chriniko.routesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;

@SpringBootApplication(
        exclude = {
                ReactiveSecurityAutoConfiguration.class
        }
)
public class RoutesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutesServiceApplication.class, args);
    }
}
