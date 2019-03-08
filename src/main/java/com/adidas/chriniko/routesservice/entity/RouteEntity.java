package com.adidas.chriniko.routesservice.entity;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@EqualsAndHashCode(callSuper = false, of = {"originCityName", "originCountry", "destinyCityName", "destinyCountry"})
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

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
