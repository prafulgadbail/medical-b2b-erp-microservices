package com.edublitz.orderservice.repository;

import com.edublitz.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByBuyerOrgId(String buyerOrgId, Pageable pageable);

    Page<Order> findByDistributorOrgId(String distributorOrgId, Pageable pageable);

    Page<Order> findByBuyerOrgIdAndStatus(String buyerOrgId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findByDistributorOrgIdAndStatus(String distributorOrgId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    long countByBuyerOrgIdAndStatus(String buyerOrgId, Order.OrderStatus status);

    long countByDistributorOrgIdAndStatus(String distributorOrgId, Order.OrderStatus status);
}
