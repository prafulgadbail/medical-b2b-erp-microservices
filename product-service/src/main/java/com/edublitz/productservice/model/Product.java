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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Product catalog entry for medicines and medical equipment.
 *
 * MongoDB Collection: products
 * Indexes:
 *   - sku (unique)
 *   - category + active (compound, for filtered catalog queries)
 *   - distributorId (for distributor-scoped queries)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@CompoundIndex(name = "category_active_idx", def = "{'category': 1, 'active': 1}")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;             // Stock Keeping Unit (unique identifier)

    private String name;
    private String genericName;     // INN for medicines
    private String description;
    private String manufacturer;

    private ProductCategory category;
    private ProductType type;

    private String dosageForm;      // Tablet, Capsule, Injection, etc. (medicines only)
    private String strength;        // e.g., "500mg", "10ml"
    private String unit;            // Each, Box, Strip, Vial

    private BigDecimal mrp;         // Maximum Retail Price
    private BigDecimal wholesalePrice;

    private boolean prescriptionRequired;
    private boolean controlledSubstance;   // Schedule H/H1/X drugs

    private String hsnCode;         // Harmonized System of Nomenclature (GST)
    private Double gstRate;

    // Regulatory
    private String drugLicenseNumber;
    private LocalDate approvalDate;

    @Indexed
    private String distributorId;   // The organization that supplies this product

    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum ProductCategory {
        MEDICINE, SURGICAL, DIAGNOSTIC, EQUIPMENT, CONSUMABLE, VACCINE
    }

    public enum ProductType {
        BRANDED, GENERIC
    }
}
