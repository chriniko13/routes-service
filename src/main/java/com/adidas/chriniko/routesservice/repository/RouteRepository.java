package com.adidas.chriniko.routesservice.repository;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Optional;


@Repository
public class RouteRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<RouteEntity> find(String originCityName, String originCountry) {

        TypedQuery<RouteEntity> tq = em.createNamedQuery("RouteEntity.findByOriginCityNameAndOriginCountry", RouteEntity.class);
        tq.setParameter("originCityName", originCityName);
        tq.setParameter("originCountry", originCountry);

        return extract(tq);
    }

    public Optional<RouteEntity> find(String originCityName, String originCountry,
                                      String destinyCityName, String destinyCountry) {

        TypedQuery<RouteEntity> tq = em.createNamedQuery("RouteEntity.findByOriginCityNameAndOriginCountryAndDestinyCityNameAndDestinyCountry", RouteEntity.class);
        tq.setParameter("originCityName", originCityName);
        tq.setParameter("originCountry", originCountry);
        tq.setParameter("destinyCityName", destinyCityName);
        tq.setParameter("destinyCountry", destinyCountry);

        return extract(tq);
    }

    public Optional<RouteEntity> find(String id) {
        return Optional.ofNullable(em.find(RouteEntity.class, id));
    }

    public void deleteAll() {
        em.createQuery("DELETE FROM RouteEntity").executeUpdate();
    }

    public void insert(RouteEntity routeEntity) {
        em.persist(routeEntity);
    }

    public RouteEntity update(RouteEntity routeEntity) {
        return em.merge(routeEntity);
    }

    private Optional<RouteEntity> extract(TypedQuery<RouteEntity> tq) {
        try {
            return Optional.of(tq.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
