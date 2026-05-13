package com.bankofgeorgia.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublic(request)) {
            return chain.filter(exchange);
        }

        Optional<JwtPrincipal> principal = bearerToken(request)
                .flatMap(jwtValidator::validate);
        if (principal.isEmpty()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        if (!isAuthorized(principal.get(), request)) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest authenticatedRequest = request.mutate()
                .header("X-Auth-Subject", principal.get().subject())
                .header("X-Auth-Role", principal.get().role())
                .build();
        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Optional<String> bearerToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authorization.substring("Bearer ".length()).trim());
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (HttpMethod.OPTIONS.equals(method) || path.startsWith("/actuator")) {
            return true;
        }

        if (!isKnownApiPath(path)) {
            return true;
        }

        if (HttpMethod.POST.equals(method)
                && (path.equals("/api/customers/register")
                || path.equals("/api/customers/login")
                || path.equals("/api/employees/login"))) {
            return true;
        }

        return HttpMethod.GET.equals(method) && path.startsWith("/api/products");
    }

    private boolean isKnownApiPath(String path) {
        return path.startsWith("/api/customers")
                || path.startsWith("/api/employees")
                || path.startsWith("/api/products")
                || path.startsWith("/api/accounts")
                || path.startsWith("/api/transactions")
                || path.startsWith("/api/notifications");
    }

    private boolean isAuthorized(JwtPrincipal principal, ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if ("EMPLOYEE".equals(principal.role())) {
            return true;
        }

        if (path.startsWith("/api/employees")) {
            return false;
        }

        if (path.startsWith("/api/products") && !HttpMethod.GET.equals(method)) {
            return false;
        }

        if (path.equals("/api/accounts") && HttpMethod.GET.equals(method)) {
            return false;
        }

        if (path.startsWith("/api/accounts/customer/")) {
            String customerId = path.substring("/api/accounts/customer/".length());
            return principal.subject().equals(customerId);
        }

        if (path.startsWith("/api/customers/")) {
            String customerId = path.substring("/api/customers/".length());
            return principal.subject().equals(customerId);
        }

        return "CUSTOMER".equals(principal.role());
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
