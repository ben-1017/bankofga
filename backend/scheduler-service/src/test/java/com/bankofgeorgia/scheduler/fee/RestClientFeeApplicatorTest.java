package com.bankofgeorgia.scheduler.fee;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientFeeApplicatorTest {

    private static final String BASE = "http://transaction-service:8084";

    @Test
    void apply_postsMonthlyFeeRequestToTransactionService() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE + "/api/transactions/internal/monthly-fee"))
                .andExpect(content().json("""
                        {
                          "accountId": "acct-1",
                          "amount": 5.00,
                          "description": "Monthly maintenance fee"
                        }
                        """))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        RestClientFeeApplicator applicator = new RestClientFeeApplicator(restTemplate, BASE);

        assertThat(applicator.apply("acct-1", new BigDecimal("5.00"))).isTrue();
        server.verify();
    }

    @Test
    void apply_returnsFalseWhenTransactionServiceRejectsFee() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE + "/api/transactions/internal/monthly-fee"))
                .andRespond(withBadRequest());

        RestClientFeeApplicator applicator = new RestClientFeeApplicator(restTemplate, BASE);

        assertThat(applicator.apply("acct-1", new BigDecimal("5.00"))).isFalse();
        server.verify();
    }
}
