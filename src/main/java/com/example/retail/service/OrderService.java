package com.example.retail.service;

import com.example.retail.client.ProductClient;
import com.example.retail.model.Order;
import com.example.retail.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final ProductClient productClient;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public Order getOrderDetails(String orderId) {
        // Static mapping: Order ID maps to productId 1001
        String productId = "1001";
        logger.info("Calling product client for product ID: {}", productId);

        Product product = productClient.getProductById(productId);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setProduct(product);
        return order;
    }
}
