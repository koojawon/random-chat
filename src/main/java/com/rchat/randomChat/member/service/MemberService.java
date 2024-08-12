package com.rchat.randomChat.member.service;

import com.rchat.randomChat.global.repository.UserRepository;
import com.rchat.randomChat.global.repository.entity.UserInfo;
import com.rchat.randomChat.member.dto.CheckEmailDto;
import com.rchat.randomChat.member.dto.SignUpDto;
import com.rchat.randomChat.security.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserInfo handleSignup(SignUpDto signupDto) {
        UserInfo userInfo = UserInfo.builder()
                .email(signupDto.getUsername())
                .pw(passwordEncoder.encode(signupDto.getPassword()))
                .nickname(signupDto.getNickname())
                .role(Roles.USER)
                .build();
        return userRepository.save(userInfo);
    }

    public boolean checkEmail(CheckEmailDto emailDto) {
        return !userRepository.existsByEmail(emailDto.getEmail());
    }
}
