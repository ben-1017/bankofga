package com.bankofgeorgia.scheduler.fee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class RestClientFeeApplicator implements FeeApplicator {

    private static final Logger log = LoggerFactory.getLogger(RestClientFeeApplicator.class);

    private final RestTemplate restTemplate;
    private final String transactionServiceUrl;

    public RestClientFeeApplicator(RestTemplate restTemplate,
                                   @Value("${transaction.service.url}") String transactionServiceUrl) {
        this.restTemplate = restTemplate;
        this.transactionServiceUrl = transactionServiceUrl;
    }

    @Override
    public boolean apply(String accountId, BigDecimal amount) {
        try {
            restTemplate.postForEntity(
                    transactionServiceUrl + "/api/transactions/internal/monthly-fee",
                    new HttpEntity<>(Map.of(
                            "accountId", accountId,
                            "amount", amount,
                            "description", "Monthly maintenance fee")),
                    Void.class);
            return true;
        } catch (RestClientException ex) {
            log.warn("failed to apply monthly fee to account {}: {}", accountId, ex.getMessage());
            return false;
        }
    }
}
