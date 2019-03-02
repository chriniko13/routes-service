package com.adidas.chriniko.routesservice.configuration;

import com.adidas.chriniko.routesservice.dto.CityInfo;
import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.adidas.chriniko.routesservice.serializer.CityInfoRedisSerializer;
import com.adidas.chriniko.routesservice.serializer.RouteInfoRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    // Note: CityInfo ---> RouteInfo
    @Bean
    public RedisTemplate<CityInfo, RouteInfo> redisTemplateCityInfoToRouteInfo(
            JedisConnectionFactory jedisConnectionFactory,
            CityInfoRedisSerializer cityInfoRedisSerializer,
            RouteInfoRedisSerializer routeInfoRedisSerializer) {

        final RedisTemplate<CityInfo, RouteInfo> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(cityInfoRedisSerializer);
        redisTemplate.setValueSerializer(routeInfoRedisSerializer);

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
