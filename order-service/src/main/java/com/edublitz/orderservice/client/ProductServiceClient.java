package com.edublitz.orderservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for inter-service communication with product-service.
 * Uses WebClient (non-blocking). Adds the forwarded JWT token for auth.
 *
 * This pattern (API-based cross-service communication) enforces the
 * strict service boundary — order-service never accesses products_db directly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.product-service.base-url}")
    private String productServiceUrl;

    /**
     * Fetch product details (name, price, GST rate) for order line item enrichment.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProduct(String productId, String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/products/" + productId)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        r -> Mono.error(new RuntimeException("Product not found: " + productId)))
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * Reserve stock for each item in an order.
     * Returns true only if reservation succeeded.
     */
    public boolean reserveStock(String productId, int quantity, String bearerToken) {
        try {
            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(productServiceUrl + "/products/inventory/reserve"
                            + "?productId=" + productId + "&quantity=" + quantity)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null && Boolean.TRUE.equals(response.get("reserved"));
        } catch (Exception e) {
            log.error("Stock reservation failed for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Release a reservation when an order is cancelled.
     */
    public void releaseStock(String productId, int quantity, String bearerToken) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(productServiceUrl + "/products/inventory/release"
                            + "?productId=" + productId + "&quantity=" + quantity)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Stock release failed for product {}: {}", productId, e.getMessage());
        }
    }

    public BigDecimal getWholesalePrice(Map<String, Object> product) {
        Object price = product.get("wholesalePrice");
        if (price instanceof Number) return new BigDecimal(price.toString());
        return BigDecimal.ZERO;
    }

    public Double getGstRate(Map<String, Object> product) {
        Object rate = product.get("gstRate");
        if (rate instanceof Number) return ((Number) rate).doubleValue();
        return 18.0;
    }
}
