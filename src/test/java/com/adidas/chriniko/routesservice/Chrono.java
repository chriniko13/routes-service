package com.adidas.chriniko.routesservice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Chrono {

    public static LocalDateTime map(Instant time) {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC).withNano(0);
    }
}
