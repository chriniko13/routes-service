package com.adidas.chriniko.routesservice.serializer;

import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2

@Component
public class RouteInfoRedisSerializer implements RedisSerializer<RouteInfo> {

    private final ObjectMapper objectMapper;

    @Autowired
    public RouteInfoRedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(RouteInfo routeInfo) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(routeInfo);
        } catch (JsonProcessingException e) {
            String msg = "could not serialize route info";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }

    @Override
    public RouteInfo deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, RouteInfo.class);
        } catch (IOException e) {
            String msg = "could not deserialize route info";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }
}
