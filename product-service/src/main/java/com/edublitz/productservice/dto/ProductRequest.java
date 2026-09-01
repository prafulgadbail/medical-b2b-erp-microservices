package com.edublitz.productservice.dto;

import com.edublitz.productservice.model.Product;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String genericName;
    private String description;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull(message = "Category is required")
    private Product.ProductCategory category;

    @NotNull(message = "Type is required")
    private Product.ProductType type;

    private String dosageForm;
    private String strength;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.01", message = "MRP must be positive")
    private BigDecimal mrp;

    @NotNull(message = "Wholesale price is required")
    @DecimalMin(value = "0.01", message = "Wholesale price must be positive")
    private BigDecimal wholesalePrice;

    private boolean prescriptionRequired;
    private boolean controlledSubstance;
    private String hsnCode;

    @Min(value = 0) @Max(value = 28)
    private Double gstRate;

    private String drugLicenseNumber;
    private LocalDate approvalDate;

    @NotBlank(message = "Distributor ID is required")
    private String distributorId;
}
