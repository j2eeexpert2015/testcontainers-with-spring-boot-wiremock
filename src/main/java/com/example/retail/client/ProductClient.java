package com.example.retail.client;

import com.example.retail.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;

    public ProductClient(@Value("${product.service.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.productServiceBaseUrl = baseUrl;
    }

    public Product getProductById(String productId) {
        String url = productServiceBaseUrl + "/api/products/" + productId;
        try {
            logger.info("Calling external product service: {}", url);
            return restTemplate.getForObject(url, Product.class);
        } catch (RestClientException ex) {
            logger.error("Error calling product service", ex);
            return null;
        }
    }
}
