package com.bankofgeorgia.scheduler.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Queries account-service for accounts eligible for the monthly maintenance fee.
 * Calls account-service directly (service-to-service), bypassing the API gateway
 * — the gateway rejects any /api/**​/internal/** path.
 */
@Component
public class RestClientAccountQueryClient implements AccountQueryClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientAccountQueryClient.class);

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public RestClientAccountQueryClient(RestTemplate restTemplate,
                                        @Value("${account.service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    @Override
    public List<AccountSnapshot> findFeeEligibleAccounts() {
        try {
            ResponseEntity<List<AccountSnapshot>> response = restTemplate.exchange(
                    accountServiceUrl + "/api/accounts/internal/fee-eligible",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AccountSnapshot>>() {});
            List<AccountSnapshot> body = response.getBody();
            return body != null ? body : List.of();
        } catch (RestClientException ex) {
            // A scheduled job must not crash when account-service is down — skip
            // this run and let the next scheduled tick retry.
            log.warn("account-service unavailable for fee-eligible query, skipping run: {}", ex.getMessage());
            return List.of();
        }
    }
}
