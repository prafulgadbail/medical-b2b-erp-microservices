package com.edublitz.productservice.repository;

import com.edublitz.productservice.model.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<InventoryItem, String> {

    List<InventoryItem> findByProductId(String productId);

    Optional<InventoryItem> findByProductIdAndWarehouseIdAndBatchNumber(
            String productId, String warehouseId, String batchNumber);

    List<InventoryItem> findByDistributorId(String distributorId);

    @Query("{ 'expiryDate': { $lte: ?0 }, 'status': { $ne: 'EXPIRED' } }")
    List<InventoryItem> findExpiringBefore(LocalDate date);

    @Query("{ 'quantityAvailable': { $lte: '$reorderLevel' } }")
    List<InventoryItem> findLowStockItems();

    List<InventoryItem> findByWarehouseId(String warehouseId);
}
