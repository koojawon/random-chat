package com.rchat.randomChat.security.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@AllArgsConstructor
@RedisHash("userRefreshToken")
public class UserRefreshToken {

    @Id
    private String email;
    private String refreshToken;
}
