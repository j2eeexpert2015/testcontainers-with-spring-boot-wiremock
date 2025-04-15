package com.example.retail.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.retail.model.Order;
import com.example.retail.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public Order getOrderWithProduct(@PathVariable String orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        return orderService.getOrderDetails(orderId);
    }
}
