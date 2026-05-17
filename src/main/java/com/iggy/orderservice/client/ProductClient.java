package com.iggy.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product-service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public boolean reduceStock(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/products/" + productId
                    + "/reduce-stock?quantity=" + quantity;
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    null,
                    Boolean.class
            );
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            return false;
        }
    }
}
