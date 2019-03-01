package com.adidas.chriniko.routesservice.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Data

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    protected String id = UUID.randomUUID().toString();

    protected String createdBy = who();
    protected Instant createdDate = when();

    protected String updatedBy = who();
    protected Instant updatedDate = when();

    @Version
    protected Long version;

    // Note: soft deletion.
    protected Boolean deleted = Boolean.FALSE;


    // -----------------------------------------------------------------------------------------------------------------
    public static String who() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    private static Clock clock = Clock.systemUTC();

    public static Instant when() {
        return Instant.now(clock);
    }
}
