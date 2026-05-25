package com.bankofgeorgia.scheduler.product;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestClientProductQueryClientTest {

    private static final String BASE = "http://product-service:8082";

    @Test
    void isCheckingProduct_returnsTrueForCheckingProducts() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE + "/api/products/prod-1"))
                .andRespond(withSuccess("{\"id\":\"prod-1\",\"type\":\"CHECKING\"}", MediaType.APPLICATION_JSON));

        RestClientProductQueryClient client = new RestClientProductQueryClient(restTemplate, BASE);

        assertThat(client.isCheckingProduct("prod-1")).isTrue();
        server.verify();
    }

    @Test
    void isCheckingProduct_returnsFalseForNonCheckingProducts() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE + "/api/products/prod-2"))
                .andRespond(withSuccess("{\"id\":\"prod-2\",\"type\":\"SAVINGS\"}", MediaType.APPLICATION_JSON));

        RestClientProductQueryClient client = new RestClientProductQueryClient(restTemplate, BASE);

        assertThat(client.isCheckingProduct("prod-2")).isFalse();
        server.verify();
    }

    @Test
    void isCheckingProduct_returnsFalseWhenLookupFails() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE + "/api/products/prod-3"))
                .andRespond(withServerError());

        RestClientProductQueryClient client = new RestClientProductQueryClient(restTemplate, BASE);

        assertThat(client.isCheckingProduct("prod-3")).isFalse();
        server.verify();
    }
}
