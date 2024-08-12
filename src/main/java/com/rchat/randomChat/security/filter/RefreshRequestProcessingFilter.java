package com.rchat.randomChat.security.filter;

import com.rchat.randomChat.global.repository.UserRepository;
import com.rchat.randomChat.redis.service.RedisService;
import com.rchat.randomChat.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RefreshRequestProcessingFilter extends OncePerRequestFilter {

    private static final String TARGET_URI = "/api/refresh";
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().contains(TARGET_URI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(e -> !redisService.getValue(e).equals("logout"))
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken == null) {
            throw new AccessDeniedException("no refresh token found");
        }
        checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        String email = redisService.getValue(refreshToken);
        jwtService.checkRefreshToken(email, refreshToken);
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = jwtService.createRefreshToken(email);
                    jwtService.sendAccessAndRefreshToken(response,
                            jwtService.createAccessToken(email),
                            reIssuedRefreshToken);
                    jwtService.updateRefreshTokenOfRepository(email, reIssuedRefreshToken);
                });
    }
}
