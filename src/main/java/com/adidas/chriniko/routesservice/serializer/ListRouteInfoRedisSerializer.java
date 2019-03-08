package com.adidas.chriniko.routesservice.serializer;

import com.adidas.chriniko.routesservice.dto.RouteInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Log4j2

@Component
public class ListRouteInfoRedisSerializer implements RedisSerializer<List<RouteInfo>> {

    private final ObjectMapper objectMapper;

    @Autowired
    public ListRouteInfoRedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(List<RouteInfo> routeInfos) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(routeInfos);
        } catch (JsonProcessingException e) {
            String msg = "could not serialize route infos";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }

    @Override
    public List<RouteInfo> deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, new TypeReference<List<RouteInfo>>() {
            });
        } catch (IOException e) {
            String msg = "could not deserialize route infos";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }
}
