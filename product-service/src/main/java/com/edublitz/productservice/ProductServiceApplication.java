package com.edublitz.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Product Service — manages product catalog, inventory levels,
 * stock tracking, and expiry monitoring for the Medical B2B ERP.
 */
@SpringBootApplication
@EnableMongoAuditing
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
