package com.rchat.randomChat.security.filter;


import com.rchat.randomChat.member.repository.UserRepository;
import com.rchat.randomChat.member.repository.entity.UserInfo;
import com.rchat.randomChat.redis.service.RedisService;
import com.rchat.randomChat.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String[] NO_CHECK_URI = {"/api/login", "/api/logout"};

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisService redisService;

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (Arrays.stream(NO_CHECK_URI).anyMatch(e -> e.equals(request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }
        checkAccessTokenAndAuthenticate(request, response, filterChain);
    }


    public void checkAccessTokenAndAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request)
                .filter(e -> !redisService.checkExistsKey(e))
                .filter(jwtService::isTokenValid)
                .flatMap(jwtService::extractEmail)
                .flatMap(userRepository::findByEmail)
                .ifPresent(this::saveAuthentication);
        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(UserInfo user) {
        String password = user.getPw();
        if (password == null) {
            password = "tempPasswordForSocialUser";
        }
        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .roles(user.getRole().name())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsUser, user,
                grantedAuthoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
