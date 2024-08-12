package com.rchat.randomChat.security.handler;

import com.rchat.randomChat.redis.service.RedisService;
import com.rchat.randomChat.security.Constants.SecurityConstants;
import com.rchat.randomChat.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtService jwtService;
    private final RedisService redisService;

    private void registerExpiredToken(String accessToken, String refreshToken) {
        redisService.setValue(SecurityConstants.ACCESS_REDIS_PREFIX + accessToken, SecurityConstants.LOGOUT_TOKEN,
                Duration.ofMillis(SecurityConstants.ACCESS_TOKEN_EXPIRATION));
        redisService.setValue(SecurityConstants.REFRESH_REDIS_PREFIX + refreshToken, SecurityConstants.LOGOUT_TOKEN,
                Duration.ofMillis(SecurityConstants.REFRESH_TOKEN_EXPIRATION));

    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        String accessToken = jwtService.extractAccessToken(request).orElseThrow(
                () -> new AccessDeniedException(SecurityConstants.ACCESS_DENIED_TOKEN)
        );
        String refreshToken = jwtService.extractRefreshToken(request).orElseThrow(
                () -> new AccessDeniedException(SecurityConstants.ACCESS_DENIED_TOKEN)
        );
        registerExpiredToken(accessToken, refreshToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("logout ok");
    }
}
