package com.bankofgeorgia.scheduler.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClientProductQueryClient implements ProductQueryClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientProductQueryClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public RestClientProductQueryClient(RestTemplate restTemplate,
                                        @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    @Override
    public boolean isCheckingProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            return false;
        }
        try {
            ProductSnapshot product = restTemplate.getForObject(
                    productServiceUrl + "/api/products/" + productId, ProductSnapshot.class);
            return product != null && "CHECKING".equals(product.type());
        } catch (RestClientException ex) {
            log.warn("product-service unavailable for product {}: {}", productId, ex.getMessage());
            return false;
        }
    }

    private record ProductSnapshot(String id, String type) {}
}
