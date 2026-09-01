package com.edublitz.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Core order document — represents a B2B purchase order from a hospital to a distributor.
 *
 * MongoDB Collection: orders
 * Indexes:
 *   - orderNumber (unique)
 *   - buyerOrgId + status (compound — dashboard queries)
 *   - distributorOrgId + status (compound — distributor operations)
 *   - createdAt (for time-range reporting)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
@CompoundIndex(name = "buyer_status_idx", def = "{'buyerOrgId': 1, 'status': 1}")
@CompoundIndex(name = "distributor_status_idx", def = "{'distributorOrgId': 1, 'status': 1}")
public class Order {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;     // Auto-generated: MED-ORD-YYYYMMDD-XXXXX

    @Indexed
    private String buyerOrgId;      // Hospital placing the order
    private String buyerOrgName;

    @Indexed
    private String distributorOrgId;
    private String distributorOrgName;

    private String createdByUserId;
    private String createdByEmail;

    private List<OrderItem> items;

    private OrderStatus status;

    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;

    private String shippingAddress;

    private String notes;
    private String rejectionReason;

    private String approvedByUserId;
    private Instant approvedAt;

    private String dispatchedByUserId;
    private Instant dispatchedAt;
    private String trackingNumber;

    private Instant deliveredAt;
    private String deliveryConfirmedBy;

    private Invoice invoice;

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum OrderStatus {
        DRAFT,          // Created but not submitted
        PENDING,        // Submitted, awaiting distributor approval
        APPROVED,       // Distributor approved
        REJECTED,       // Distributor rejected
        PROCESSING,     // Stock reserved, being packed
        DISPATCHED,     // Shipped
        DELIVERED,      // Confirmed received
        CANCELLED       // Cancelled by hospital
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productSku;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal gstRate;
        private BigDecimal lineTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Invoice {
        private String invoiceNumber;
        private Instant invoiceDate;
        private BigDecimal subtotal;
        private BigDecimal gstAmount;
        private BigDecimal totalAmount;
        private String paymentTerms;
        private String paymentStatus;   // PENDING, PAID (simulated — no gateway)
    }
}
