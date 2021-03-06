package com.adidas.chriniko.routesservice.configuration;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.serializer.CityInfoRedisSerializer;
import com.adidas.chriniko.routesservice.serializer.ListRouteInfoRedisSerializer;
import com.adidas.chriniko.routesservice.serializer.RouteInfoRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfiguration {

    // Note: CityInfo ---> [RouteInfo]
    @Bean
    public RedisTemplate<CityInfo, List<RouteInfo>> redisTemplateCityInfoToRouteInfo(
            JedisConnectionFactory jedisConnectionFactory,
            CityInfoRedisSerializer cityInfoRedisSerializer,
            ListRouteInfoRedisSerializer listRouteInfoRedisSerializer) {

        final RedisTemplate<CityInfo, List<RouteInfo>> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(cityInfoRedisSerializer);
        redisTemplate.setValueSerializer(listRouteInfoRedisSerializer);

        redisTemplate.setConnectionFactory(jedisConnectionFactory);

        return redisTemplate;
    }

    // Note: routeId: String ---> RouteInfo
    @Bean
    public RedisTemplate<String, RouteInfo> redisTemplateRouteIdToRouteInfo(
            JedisConnectionFactory jedisConnectionFactory,
            RouteInfoRedisSerializer routeInfoRedisSerializer) {

        final RedisTemplate<String, RouteInfo> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(routeInfoRedisSerializer);

        redisTemplate.setConnectionFactory(jedisConnectionFactory);

        return redisTemplate;
    }
}
