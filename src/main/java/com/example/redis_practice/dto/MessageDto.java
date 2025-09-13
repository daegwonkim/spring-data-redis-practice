package com.example.redis_practice.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record MessageDto(
        String channel, // 타겟 채널
        String sender,   // 메시지 발신자
        String message  // 전송할 메시지 내용
) implements Serializable {
}
