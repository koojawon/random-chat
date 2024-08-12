package com.rchat.randomChat.member.dto;

import com.rchat.randomChat.member.repository.entity.UserInfo;
import lombok.Data;

@Data
public class UserInfoResponse {
    private String nickname;
    private String userId;

    public UserInfoResponse(UserInfo userInfo) {
        this.nickname = userInfo.getNickname();
        this.userId = userInfo.getEmail();
    }
}
