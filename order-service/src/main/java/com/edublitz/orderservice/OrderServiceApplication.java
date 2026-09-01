package com.edublitz.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Order Service — manages the full lifecycle of B2B medical supply orders,
 * billing simulation, and order history for the Medical B2B ERP platform.
 */
@SpringBootApplication
@EnableMongoAuditing
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
