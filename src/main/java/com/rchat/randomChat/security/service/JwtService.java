package com.rchat.randomChat.security.service;


import com.rchat.randomChat.member.repository.UserRepository;
import com.rchat.randomChat.redis.service.RedisService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class JwtService {

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "eml";
    private static final String BEARER = "Bearer ";
    private static final String REFRESH_PREFIX = "refreshInfo";
    private final UserRepository userRepository;
    private final RedisService redisService;
    @Value("${jwt.secretKey}")
    private String secretKey;
    private SecretKey encodedKey;
    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @PostConstruct
    public void initKey() {
        this.encodedKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT)
                .expiration(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .plusMillis(accessTokenExpirationPeriod)))
                .issuedAt(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .issuer("ranChat")
                .claim(EMAIL_CLAIM, email)
                .signWith(encodedKey)
                .compact();
    }

    public String createRefreshToken(String email) {
        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .expiration(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .plusMillis(refreshTokenExpirationPeriod)))
                .issuedAt(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .issuer("ranChat")
                .claim(EMAIL_CLAIM, email)
                .signWith(encodedKey)
                .compact();
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
        response.setHeader(refreshHeader, refreshToken);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(t -> t.startsWith(BEARER))
                .map(t -> t.replace(BEARER, ""));
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(t -> t.startsWith(BEARER))
                .map(t -> t.replace(BEARER, ""));
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(encodedKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload()
                    .get(EMAIL_CLAIM)
                    .toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void updateRefreshTokenOfRepository(String email, String refreshToken) {
        redisService.setValue(REFRESH_PREFIX + email, refreshToken, Duration.ofMillis(refreshTokenExpirationPeriod));
    }

    public boolean isTokenValid(String accessToken) {
        try {
            Jwts.parser()
                    .verifyWith(encodedKey)
                    .build()
                    .parseSignedClaims(accessToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void checkRefreshToken(String email, String refreshToken) throws AccessDeniedException {
        String savedToken = redisService.getValue(REFRESH_PREFIX + email);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new AccessDeniedException("invalid token");
        }
    }
}
