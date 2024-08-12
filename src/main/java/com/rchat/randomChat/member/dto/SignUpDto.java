package com.rchat.randomChat.member.dto;

import lombok.Data;

@Data
public class SignUpDto {
    private final String username;
    private final String password;
    private final String nickname;
}
