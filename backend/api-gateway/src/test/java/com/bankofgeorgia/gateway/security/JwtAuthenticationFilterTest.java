package com.bankofgeorgia.gateway.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "header.payload.signature";

    @Mock
    private JwtValidator jwtValidator;

    /** Captures the exchange the filter forwards; stays null when the filter rejects. */
    private ServerWebExchange forwarded;

    private final GatewayFilterChain chain = exchange -> {
        this.forwarded = exchange;
        return Mono.empty();
    };

    private MockServerWebExchange dispatch(HttpMethod method, String path, String bearerToken) {
        MockServerHttpRequest.BodyBuilder builder = MockServerHttpRequest.method(method, path);
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        MockServerWebExchange exchange = MockServerWebExchange.from(builder.build());
        forwarded = null;
        new JwtAuthenticationFilter(jwtValidator).filter(exchange, chain).block();
        return exchange;
    }

    private void principal(String role, String subject) {
        lenient().when(jwtValidator.validate(TOKEN))
                .thenReturn(Optional.of(new JwtPrincipal(subject, role)));
    }

    private void assertRejected(MockServerWebExchange exchange, HttpStatus expected) {
        assertThat(forwarded).as("request must not be forwarded").isNull();
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        assertThat(status).isEqualTo(expected);
    }

    private void assertForwardedAnonymously() {
        assertThat(forwarded).as("public request must be forwarded").isNotNull();
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-Auth-Subject")).isNull();
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-Auth-Role")).isNull();
    }

    private void assertForwardedAs(String subject, String role) {
        assertThat(forwarded).as("authorized request must be forwarded").isNotNull();
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-Auth-Subject")).isEqualTo(subject);
        assertThat(forwarded.getRequest().getHeaders().getFirst("X-Auth-Role")).isEqualTo(role);
    }

    // ---- public paths: forwarded without authentication ----

    @Test
    void registerIsPublic() {
        dispatch(HttpMethod.POST, "/api/customers/register", null);
        assertForwardedAnonymously();
    }

    @Test
    void loginIsPublic() {
        dispatch(HttpMethod.POST, "/api/customers/login", null);
        assertForwardedAnonymously();
    }

    @Test
    void productCatalogGetIsPublic() {
        dispatch(HttpMethod.GET, "/api/products/prod-1", null);
        assertForwardedAnonymously();
    }

    @Test
    void corsPreflightIsPublic() {
        dispatch(HttpMethod.OPTIONS, "/api/accounts/acc-1", null);
        assertForwardedAnonymously();
    }

    @Test
    void unknownNonApiPathIsPublic() {
        dispatch(HttpMethod.GET, "/favicon.ico", null);
        assertForwardedAnonymously();
    }

    // ---- authentication failures ----

    @Test
    void protectedPathWithoutTokenIsUnauthorized() {
        assertRejected(dispatch(HttpMethod.GET, "/api/accounts/acc-1", null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedPathWithInvalidTokenIsUnauthorized() {
        // jwtValidator returns empty by default for an unrecognized token
        assertRejected(dispatch(HttpMethod.GET, "/api/accounts/acc-1", "bogus"), HttpStatus.UNAUTHORIZED);
    }

    // ---- internal service-to-service endpoints are never proxied ----

    @Test
    void internalPathIsForbiddenForEmployee() {
        principal("EMPLOYEE", "emp-1");
        assertRejected(
                dispatch(HttpMethod.GET, "/api/accounts/internal/fee-eligible", TOKEN),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void internalPathIsForbiddenForCustomer() {
        principal("CUSTOMER", "cust-1");
        assertRejected(
                dispatch(HttpMethod.GET, "/api/accounts/internal/fee-eligible", TOKEN),
                HttpStatus.FORBIDDEN);
    }

    // ---- employee: full access, identity headers injected ----

    @Test
    void employeeMayListAllAccountsAndIsTagged() {
        principal("EMPLOYEE", "emp-1");
        dispatch(HttpMethod.GET, "/api/accounts", TOKEN);
        assertForwardedAs("emp-1", "EMPLOYEE");
    }

    @Test
    void employeeMayCreateProduct() {
        principal("EMPLOYEE", "emp-1");
        dispatch(HttpMethod.POST, "/api/products", TOKEN);
        assertForwardedAs("emp-1", "EMPLOYEE");
    }

    // ---- customer: ownership-scoped access ----

    @Test
    void customerMayReadOwnCustomerRecord() {
        principal("CUSTOMER", "cust-1");
        dispatch(HttpMethod.GET, "/api/customers/cust-1", TOKEN);
        assertForwardedAs("cust-1", "CUSTOMER");
    }

    @Test
    void customerMayNotReadAnotherCustomerRecord() {
        principal("CUSTOMER", "cust-1");
        assertRejected(dispatch(HttpMethod.GET, "/api/customers/cust-2", TOKEN), HttpStatus.FORBIDDEN);
    }

    @Test
    void customerMayListOwnAccounts() {
        principal("CUSTOMER", "cust-1");
        dispatch(HttpMethod.GET, "/api/accounts/customer/cust-1", TOKEN);
        assertForwardedAs("cust-1", "CUSTOMER");
    }

    @Test
    void customerMayNotListAnotherCustomersAccounts() {
        principal("CUSTOMER", "cust-1");
        assertRejected(
                dispatch(HttpMethod.GET, "/api/accounts/customer/cust-2", TOKEN),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void customerMayNotListAllAccounts() {
        principal("CUSTOMER", "cust-1");
        assertRejected(dispatch(HttpMethod.GET, "/api/accounts", TOKEN), HttpStatus.FORBIDDEN);
    }

    @Test
    void customerMayNotMutateProducts() {
        principal("CUSTOMER", "cust-1");
        assertRejected(dispatch(HttpMethod.POST, "/api/products", TOKEN), HttpStatus.FORBIDDEN);
    }

    @Test
    void customerMayReadOwnNotifications() {
        principal("CUSTOMER", "cust-1");
        dispatch(HttpMethod.GET, "/api/notifications/customer/cust-1", TOKEN);
        assertForwardedAs("cust-1", "CUSTOMER");
    }

    @Test
    void customerMayNotReadAnotherCustomersNotifications() {
        principal("CUSTOMER", "cust-1");
        assertRejected(
                dispatch(HttpMethod.GET, "/api/notifications/customer/cust-2", TOKEN),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void customerMayNotReachEmployeeEndpoints() {
        principal("CUSTOMER", "cust-1");
        assertRejected(dispatch(HttpMethod.GET, "/api/employees/customers", TOKEN), HttpStatus.FORBIDDEN);
    }
}
