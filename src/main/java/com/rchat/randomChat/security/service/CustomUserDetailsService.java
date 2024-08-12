package com.rchat.randomChat.security.service;

import com.rchat.randomChat.member.repository.UserRepository;
import com.rchat.randomChat.member.repository.entity.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userRepository.findByEmail(username).orElseThrow();
        return org.springframework.security.core.userdetails.User.builder()
                .username(userInfo.getEmail())
                .password(userInfo.getPw())
                .roles(userInfo.getRole().toString())
                .build();
    }
}
