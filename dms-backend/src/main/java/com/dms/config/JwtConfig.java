package com.dms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /** Base64-encoded HMAC-SHA512 secret (min 512 bits recommended). */
    private String secret;

    /** Access token validity in milliseconds (default 2 hours). */
    private Long expiration = 7_200_000L;

    /** Refresh token validity in milliseconds (default 7 days). */
    private Long refreshExpiration = 604_800_000L;

    /** Value placed in the {@code iss} claim. */
    private String issuer = "DMS-Application";

    /** HTTP header name carrying the token. */
    private String header = "Authorization";

    /** Token scheme prefix including trailing space. */
    private String prefix = "Bearer ";
}