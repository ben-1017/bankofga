package com.bankofgeorgia.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class CustomerLookup {

    private static final Logger log = LoggerFactory.getLogger(CustomerLookup.class);

    public record Contact(String email, String phone) {}

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public CustomerLookup(RestTemplateBuilder builder,
                          @Value("${customer.service.url:http://localhost:8181}") String customerServiceUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
        this.customerServiceUrl = customerServiceUrl;
    }

    public Contact resolve(String customerId) {
        try {
            CustomerResponse c = restTemplate.getForObject(
                    customerServiceUrl + "/api/customers/" + customerId, CustomerResponse.class);
            if (c == null) return null;
            return new Contact(c.email(), c.phone());
        } catch (RestClientException ex) {
            log.warn("customer lookup failed for {}: {}", customerId, ex.getMessage());
            return null;
        }
    }

    private record CustomerResponse(String id, String name, String email, String username, String phone) {}
}
