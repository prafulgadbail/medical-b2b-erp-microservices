package com.edublitz.productservice.service;

import com.edublitz.productservice.dto.ProductRequest;
import com.edublitz.productservice.dto.StockUpdateRequest;
import com.edublitz.productservice.exception.BadRequestException;
import com.edublitz.productservice.exception.ResourceNotFoundException;
import com.edublitz.productservice.model.InventoryItem;
import com.edublitz.productservice.model.Product;
import com.edublitz.productservice.repository.InventoryRepository;
import com.edublitz.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Value("${stock.low-threshold:10}")
    private int lowStockThreshold;

    public Product createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .genericName(request.getGenericName())
                .description(request.getDescription())
                .manufacturer(request.getManufacturer())
                .category(request.getCategory())
                .type(request.getType())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .unit(request.getUnit())
                .mrp(request.getMrp())
                .wholesalePrice(request.getWholesalePrice())
                .prescriptionRequired(request.isPrescriptionRequired())
                .controlledSubstance(request.isControlledSubstance())
                .hsnCode(request.getHsnCode())
                .gstRate(request.getGstRate())
                .drugLicenseNumber(request.getDrugLicenseNumber())
                .distributorId(request.getDistributorId())
                .active(true)
                .build();

        return productRepository.save(product);
    }

    public Page<Product> listProducts(String category, String distributorId, Pageable pageable) {
        if (category != null) {
            Product.ProductCategory cat = Product.ProductCategory.valueOf(category.toUpperCase());
            return productRepository.findByCategoryAndActiveTrue(cat, pageable);
        }
        if (distributorId != null) {
            return productRepository.findByDistributorIdAndActiveTrue(distributorId, pageable);
        }
        return productRepository.findByActiveTrue(pageable);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.searchByName(query, pageable);
    }

    public InventoryItem addStock(StockUpdateRequest request) {
        // Ensure the product exists
        Product product = getProductById(request.getProductId());

        return inventoryRepository
                .findByProductIdAndWarehouseIdAndBatchNumber(
                        request.getProductId(), request.getWarehouseId(), request.getBatchNumber())
                .map(existing -> {
                    existing.setQuantityAvailable(existing.getQuantityAvailable() + request.getQuantity());
                    existing.setStatus(computeStatus(existing));
                    return inventoryRepository.save(existing);
                })
                .orElseGet(() -> {
                    InventoryItem item = InventoryItem.builder()
                            .productId(request.getProductId())
                            .productSku(product.getSku())
                            .productName(product.getName())
                            .warehouseId(request.getWarehouseId())
                            .warehouseLocation(request.getWarehouseLocation())
                            .batchNumber(request.getBatchNumber())
                            .manufacturingDate(request.getManufacturingDate())
                            .expiryDate(request.getExpiryDate())
                            .quantityAvailable(request.getQuantity())
                            .quantityReserved(0)
                            .reorderLevel(request.getReorderLevel())
                            .distributorId(request.getDistributorId())
                            .status(InventoryItem.StockStatus.IN_STOCK)
                            .build();
                    return inventoryRepository.save(item);
                });
    }

    public List<InventoryItem> getInventoryForProduct(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<InventoryItem> getExpiringItems(int daysAhead) {
        LocalDate cutoff = LocalDate.now().plusDays(daysAhead);
        return inventoryRepository.findExpiringBefore(cutoff);
    }

    /**
     * Reserve stock for an order. Called by order-service via API.
     * Returns false if insufficient stock is available.
     */
    public boolean reserveStock(String productId, String warehouseId, int quantity) {
        List<InventoryItem> items = inventoryRepository.findByProductId(productId);
        for (InventoryItem item : items) {
            if ((warehouseId == null || item.getWarehouseId().equals(warehouseId))
                    && item.getQuantityOnHand() >= quantity) {
                item.setQuantityReserved(item.getQuantityReserved() + quantity);
                item.setStatus(computeStatus(item));
                inventoryRepository.save(item);
                return true;
            }
        }
        return false;
    }

    /**
     * Release a previously reserved quantity (e.g., order cancelled).
     */
    public void releaseReservation(String productId, int quantity) {
        inventoryRepository.findByProductId(productId).stream()
                .filter(i -> i.getQuantityReserved() >= quantity)
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantityReserved(Math.max(0, item.getQuantityReserved() - quantity));
                    item.setStatus(computeStatus(item));
                    inventoryRepository.save(item);
                });
    }

    private InventoryItem.StockStatus computeStatus(InventoryItem item) {
        if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
            return InventoryItem.StockStatus.EXPIRED;
        }
        int onHand = item.getQuantityOnHand();
        if (onHand <= 0) return InventoryItem.StockStatus.OUT_OF_STOCK;
        if (onHand <= lowStockThreshold) return InventoryItem.StockStatus.LOW_STOCK;
        return InventoryItem.StockStatus.IN_STOCK;
    }
}
