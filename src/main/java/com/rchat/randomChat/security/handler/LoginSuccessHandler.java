package com.rchat.randomChat.security.handler;

import com.rchat.randomChat.redis.service.RedisService;
import com.rchat.randomChat.security.Constants.SecurityConstants;
import com.rchat.randomChat.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RedisService redisService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String email = extractUserName(authentication);
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken(email);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        redisService.setValue(SecurityConstants.REFRESH_REDIS_PREFIX + refreshToken, email,
                Duration.ofMillis(SecurityConstants.REFRESH_TOKEN_EXPIRATION));
    }

    private String extractUserName(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
