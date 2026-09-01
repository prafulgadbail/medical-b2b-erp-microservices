package com.edublitz.productservice.controller;

import com.edublitz.productservice.dto.ProductRequest;
import com.edublitz.productservice.dto.StockUpdateRequest;
import com.edublitz.productservice.model.InventoryItem;
import com.edublitz.productservice.model.Product;
import com.edublitz.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping
    @Operation(summary = "List products (filterable by category and distributor)")
    public ResponseEntity<Page<Product>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String distributorId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.listProducts(category, distributorId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across product names")
    public ResponseEntity<Page<Product>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    public ResponseEntity<Product> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR')")
    @Operation(summary = "Add or update stock for a product batch")
    public ResponseEntity<InventoryItem> addStock(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addStock(request));
    }

    @GetMapping("/{id}/inventory")
    @Operation(summary = "Get inventory details for a specific product")
    public ResponseEntity<List<InventoryItem>> getInventory(@PathVariable String id) {
        return ResponseEntity.ok(productService.getInventoryForProduct(id));
    }

    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR')")
    @Operation(summary = "Get all low-stock items")
    public ResponseEntity<List<InventoryItem>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockItems());
    }

    @GetMapping("/inventory/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISTRIBUTOR')")
    @Operation(summary = "Get items expiring within N days")
    public ResponseEntity<List<InventoryItem>> getExpiring(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(productService.getExpiringItems(days));
    }

    /**
     * Internal API endpoint — called by order-service to reserve stock.
     * Not exposed externally via ALB Ingress.
     */
    @PostMapping("/inventory/reserve")
    @Operation(summary = "[Internal] Reserve stock for an order")
    public ResponseEntity<Map<String, Boolean>> reserveStock(
            @RequestParam String productId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam int quantity
    ) {
        boolean reserved = productService.reserveStock(productId, warehouseId, quantity);
        return ResponseEntity.ok(Map.of("reserved", reserved));
    }

    @PostMapping("/inventory/release")
    @Operation(summary = "[Internal] Release reserved stock")
    public ResponseEntity<Void> releaseReservation(
            @RequestParam String productId,
            @RequestParam int quantity
    ) {
        productService.releaseReservation(productId, quantity);
        return ResponseEntity.noContent().build();
    }
}
