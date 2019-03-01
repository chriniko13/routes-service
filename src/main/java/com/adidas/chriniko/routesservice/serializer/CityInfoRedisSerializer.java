package com.adidas.chriniko.routesservice.serializer;

import com.adidas.chriniko.routesservice.dto.CityInfo;
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
public class CityInfoRedisSerializer implements RedisSerializer<CityInfo> {

    private final ObjectMapper objectMapper;

    @Autowired
    public CityInfoRedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(CityInfo cityInfo) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(cityInfo);
        } catch (JsonProcessingException e) {
            String msg = "could not serialize city info";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }

    @Override
    public CityInfo deserialize(byte[] bytes) throws SerializationException {
        try {
            return objectMapper.readValue(bytes, CityInfo.class);
        } catch (IOException e) {
            String msg = "could not deserialize city info";
            log.error(msg, e);
            throw new SerializationException(msg, e);
        }
    }
}
