package com.adidas.chriniko.routesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RouteInfo {

    private String id;

    private CityInfo city;
    private CityInfo destinyCity;

    private Instant departureTime;
    private Instant arrivalTime;
}
