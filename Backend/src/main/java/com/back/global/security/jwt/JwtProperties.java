package com.back.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Duration accessTokenExpiration;
    private Duration refreshTokenExpiration;

    public long getAccessTokenExpirationMillis() {
        return accessTokenExpiration.toMillis();
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpiration.toMillis();
    }

    public int getAccessTokenCookieMaxAge() {
        return (int) accessTokenExpiration.getSeconds();
    }

    public int getRefreshTokenCookieMaxAge() {
        return (int) refreshTokenExpiration.getSeconds();
    }
}
