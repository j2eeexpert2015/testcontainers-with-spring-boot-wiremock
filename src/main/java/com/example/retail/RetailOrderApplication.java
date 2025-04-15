package com.example.retail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Retail Order Service application.
 */
@SpringBootApplication
public class RetailOrderApplication {

    private static final Logger logger = LoggerFactory.getLogger(RetailOrderApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Retail Order Service Application...");
        SpringApplication.run(RetailOrderApplication.class, args);
    }
}
