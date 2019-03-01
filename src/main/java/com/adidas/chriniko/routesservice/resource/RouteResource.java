package com.adidas.chriniko.routesservice.resource;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.service.RouteService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Log4j2

@RestController
@RequestMapping("/api/route-info")
public class RouteResource {

    private final RouteService routeService;

    @Autowired
    public RouteResource(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping(
            path = "/search",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<RouteInfo> find(@RequestBody @Valid CityInfo input) {
        log.debug("  >> search: {}", input);
        return routeService.find(input);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<RouteInfo> create(@RequestBody @Valid RouteInfo input) {
        log.debug("  >> create: {}", input);
        return routeService.create(input);
    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<RouteInfo> update(@PathVariable("id") String id, @RequestBody @Valid RouteInfo input) {
        log.debug("  >> update, id: {}, input: {}", id, input);
        return routeService.update(id, input);
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public @ResponseBody
    Mono<RouteInfo> get(@PathVariable("id") String id) {
        log.debug("  >> get: {}", id);
        return routeService.find(id);
    }

}
