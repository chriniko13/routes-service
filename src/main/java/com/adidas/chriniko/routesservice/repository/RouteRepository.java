package com.adidas.chriniko.routesservice.repository;

import com.adidas.chriniko.routesservice.entity.RouteEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public class RouteRepository {

    private static final int BATCH_SIZE = 25;

    @PersistenceContext
    private EntityManager em;

    public Optional<List<RouteEntity>> findByOrigin(String originCityName, String originCountry) {

        TypedQuery<RouteEntity> tq = em.createNamedQuery("RouteEntity.findByOriginCityNameAndOriginCountry", RouteEntity.class);
        tq.setParameter("originCityName", originCityName);
        tq.setParameter("originCountry", originCountry);

        return Optional.ofNullable(tq.getResultList());
    }

    public Optional<RouteEntity> find(String id) {
        return Optional.ofNullable(em.find(RouteEntity.class, id));
    }

    public void insert(RouteEntity routeEntity) {
        em.persist(routeEntity);
    }

    public void batchInsert(Collection<RouteEntity> routes) {
        final List<RouteEntity> savedEntities = new ArrayList<>(routes.size());

        int i = 0;

        for (RouteEntity route : routes) {

            savedEntities.add(persistOrMerge(route));
            i++;

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();
    }

    public void delete(RouteEntity routeEntity) {
        em.remove(em.merge(routeEntity));
    }

    public RouteEntity update(RouteEntity routeEntity) {
        return em.merge(routeEntity);
    }

    private RouteEntity persistOrMerge(RouteEntity route) {
        if (route.getId() == null) {
            em.persist(route);
            return route;
        } else {
            return em.merge(route);
        }
    }
}
