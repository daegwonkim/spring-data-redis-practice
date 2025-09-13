package com.example.redis_practice;

import com.example.redis_practice.dto.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
            MessageDto messageDto = objectMapper.readValue(publishMessage, MessageDto.class);

            log.info("Redis Subscribe Channel: {}", messageDto.channel());
            log.info("Redis Channel Message: {}", messageDto.message());

            // TODO
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
