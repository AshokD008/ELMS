package com.lms.usermanagementservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;

    private long accessTokenExpiration;

    private long refreshTokenExpiration;

    private String issuer;

    private String header;

    private String prefix;
}