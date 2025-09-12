package com.example.redis_practice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RedisPracticeApplicationTests {

	@Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testStrings() {
        // given
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = "key";

        // when
        ops.set(key, "value");

        // then
        String value = ops.get(key);
        assertThat(value).isEqualTo("value");
    }
}
