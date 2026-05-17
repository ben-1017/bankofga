package com.bankofgeorgia.gateway.security;

public record JwtPrincipal(
        String subject,
        String role
) {}
