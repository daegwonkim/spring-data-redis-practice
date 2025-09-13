package com.example.redis_practice;

import com.example.redis_practice.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Object publish
     *
     * @param topic 메시지를 전송할 채널
     * @param message 전송할 메시지
     */
    public void publish(ChannelTopic topic, MessageDto message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }

    /**
     * String publish
     *
     * @param topic 메시지를 전송할 채널
     * @param message 전송할 메시지
     */
    public void publish(ChannelTopic topic, String message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
