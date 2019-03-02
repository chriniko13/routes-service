package com.adidas.chriniko.routesservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)

@Entity
@Table(name = "routes")

@EntityListeners(AuditingEntityListener.class)

@NamedQueries(
        value = {
                @NamedQuery(
                        name = "RouteEntity.findByOriginCityNameAndOriginCountry",
                        query = "SELECT rE FROM RouteEntity rE " +
                                "WHERE rE.originCityName = :originCityName " +
                                "AND rE.originCountry = :originCountry"
                ),
                @NamedQuery(
                        name = "RouteEntity.findByOriginCityNameAndOriginCountryAndDestinyCityNameAndDestinyCountry",
                        query = "SELECT rE FROM RouteEntity rE " +
                                "WHERE rE.originCityName = :originCityName " +
                                "AND rE.originCountry = :originCountry " +
                                "AND rE.destinyCityName = :destinyCityName " +
                                "AND rE.destinyCountry = :destinyCountry"
                )
        }
)
public class RouteEntity extends BaseEntity {

    private String originCityName;
    private String originCountry;

    private String destinyCityName;
    private String destinyCountry;

    private Instant departureTime;

    private Instant arrivalTime;

}