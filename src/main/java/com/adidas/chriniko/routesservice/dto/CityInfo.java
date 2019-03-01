package com.adidas.chriniko.routesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class CityInfo {

    @NotBlank
    private String name;

    @NotBlank
    private String country;

}
