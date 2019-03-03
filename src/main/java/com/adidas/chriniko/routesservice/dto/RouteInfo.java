package com.adidas.chriniko.routesservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteInfo {

    private String id;

    private CityInfo city;
    private CityInfo destinyCity;

    @JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss", timezone = "UTC")
    private Instant departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss", timezone = "UTC")
    private Instant arrivalTime;
}
