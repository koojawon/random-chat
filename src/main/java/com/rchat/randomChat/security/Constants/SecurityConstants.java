package com.rchat.randomChat.security.Constants;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {

    public static String LOGOUT_TOKEN = "logout";
    public static String ACCESS_DENIED_TOKEN = "invalid token";
    @Value("${jwt.access.expiration}")
    public static Long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    public static Long REFRESH_TOKEN_EXPIRATION;

    public static String ACCESS_REDIS_PREFIX = "accessInfo";
    public static String REFRESH_REDIS_PREFIX = "refreshInfo";
}
