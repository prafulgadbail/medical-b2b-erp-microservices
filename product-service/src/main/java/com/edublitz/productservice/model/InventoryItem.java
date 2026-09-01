package com.edublitz.productservice.model;

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

import java.time.Instant;
import java.time.LocalDate;

/**
 * Tracks stock levels per product per warehouse/location.
 * Separate from Product to allow the same SKU at multiple locations.
 *
 * MongoDB Collection: inventory
 * Indexes:
 *   - productId + warehouseId (compound, unique batch tracking)
 *   - expiryDate (for expiry alerts)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
@CompoundIndex(name = "product_warehouse_batch_idx",
               def = "{'productId': 1, 'warehouseId': 1, 'batchNumber': 1}",
               unique = true)
public class InventoryItem {

    @Id
    private String id;

    @Indexed
    private String productId;

    private String productSku;
    private String productName;

    @Indexed
    private String warehouseId;

    private String warehouseLocation;

    private String batchNumber;
    private LocalDate manufacturingDate;

    @Indexed
    private LocalDate expiryDate;       // Indexed for expiry report queries

    private int quantityAvailable;
    private int quantityReserved;       // Reserved for pending orders
    private int reorderLevel;           // Trigger restocking alert below this

    private String distributorId;

    private StockStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public int getQuantityOnHand() {
        return quantityAvailable - quantityReserved;
    }

    public boolean isLowStock(int threshold) {
        return getQuantityOnHand() <= threshold;
    }

    public enum StockStatus {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK, EXPIRED, QUARANTINED
    }
}
