package com.bankofgeorgia.scheduler.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientAccountQueryClientTest {

    private static final String BASE = "http://account-service:8083";

    private MockRestServiceServer server;
    private RestClientAccountQueryClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new RestClientAccountQueryClient(restTemplate, BASE);
    }

    @Test
    void mapsFeeEligibleResponseToSnapshots() {
        server.expect(requestTo(BASE + "/api/accounts/internal/fee-eligible"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"accountId\":\"a1\",\"customerId\":\"c1\"},"
                                + "{\"accountId\":\"a2\",\"customerId\":\"c2\"}]",
                        MediaType.APPLICATION_JSON));

        List<AccountSnapshot> result = client.findFeeEligibleAccounts();

        assertThat(result).containsExactly(
                new AccountSnapshot("a1", "c1"),
                new AccountSnapshot("a2", "c2"));
        server.verify();
    }

    @Test
    void returnsEmptyList_whenAccountServiceUnavailable() {
        server.expect(requestTo(BASE + "/api/accounts/internal/fee-eligible"))
                .andRespond(withServerError());

        List<AccountSnapshot> result = client.findFeeEligibleAccounts();

        assertThat(result).isEmpty();
    }
}
