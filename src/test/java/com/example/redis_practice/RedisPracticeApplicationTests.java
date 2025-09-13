package com.example.redis_practice;

import com.example.redis_practice.dto.MessageDto;
import com.example.redis_practice.entity.User;
import com.example.redis_practice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisPracticeApplicationTests {

	@Autowired private RedisTemplate<String, String> redisTemplate;

    @Autowired private UserService userService;

    @Autowired private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired private RedisPublisher redisPublisher;
    @Autowired private RedisSubscriber redisSubscriber;


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

    @Test
    void redisCacheTest() {
        // 생성
        User user = userService.createUser("김철수", 20);

        // 조회
        Long userId = user.getId();
        User getUser = userService.getUser(userId);
        assertEquals(userId, getUser.getId());

        // 수정
        User updateUser = userService.updateUser(user.getId(), "김철수", 21);
        assertEquals(21, updateUser.getAge());

        // 삭제
        Long deletedUserId = userService.deleteUser(userId);
        assertEquals(deletedUserId, user.getId());
    }

    @Test
    void redisPubSubTest() {
        String channel = "channel1";
        MessageDto message = MessageDto.builder()
                .channel(channel)
                .sender("sender")
                .message("hello world")
                .build();

        redisMessageListenerContainer.addMessageListener(redisSubscriber, new ChannelTopic(channel));
        redisPublisher.publish(new ChannelTopic(channel), message);
    }
}
