package com.bankofgeorgia.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bankofga.jwt")
public class JwtProperties {

    private String issuer = "bankofga";
    private String secret = "dev-bankofga-jwt-secret-change-me-at-least-32";

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
