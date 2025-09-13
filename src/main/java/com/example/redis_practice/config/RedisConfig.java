package com.example.redis_practice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 환경 설정
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private Integer port;

    /**
     * Redis 서버와의 연결을 관리하는 ConnectionFactory 생성
     *
     * @return RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis 데이터 처리를 위한 템플릿 구성
     *
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // ConnectionFactory 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    /**
     *  Redis pub/sub 메시지 처리를 위한 리스너 생성
     *
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        return container;
    }

    /**
     * Redis를 캐시 저장소로 활용하기 위한 CacheManager 설정
     *
     * @param redisConnectionFactory RedisConnectionFactory
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig() // Redis 캐시의 기본 설정을 가져옴
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new StringRedisSerializer())) // 캐시 키를 문자열 형태로 직렬화하여 Redis에 저장
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())) // 캐시 값을 JSON 형태로 직렬화하여 Redis에 저장
                .entryTtl(Duration.ofMinutes(1L)); // 캐시 데이터 유효 시을 1분으로 설정

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
