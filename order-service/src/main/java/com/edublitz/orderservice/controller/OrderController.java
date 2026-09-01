package com.edublitz.orderservice.controller;

import com.edublitz.orderservice.dto.CreateOrderRequest;
import com.edublitz.orderservice.model.Order;
import com.edublitz.orderservice.security.JwtService;
import com.edublitz.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle management — create, approve, dispatch, deliver")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL')")
    @Operation(summary = "Create a new purchase order (Hospital only)")
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        String orgId = jwtService.extractOrgId(token);

        Order order = orderService.createOrder(request, userId,
                userDetails.getUsername(), orgId, null, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<Order> getByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('HOSPITAL')")
    @Operation(summary = "Get orders placed by my organization")
    public ResponseEntity<Page<Order>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String token = extractToken(httpRequest);
        String orgId = jwtService.extractOrgId(token);
        return ResponseEntity.ok(orderService.getOrdersByBuyer(orgId, pageable));
    }

    @GetMapping("/incoming")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ADMIN')")
    @Operation(summary = "Get incoming orders for my distributor org")
    public ResponseEntity<Page<Order>> getIncomingOrders(
            HttpServletRequest httpRequest,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String token = extractToken(httpRequest);
        String orgId = jwtService.extractOrgId(token);
        return ResponseEntity.ok(orderService.getOrdersByDistributor(orgId, pageable));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ADMIN')")
    @Operation(summary = "Approve a pending order")
    public ResponseEntity<Order> approve(
            @PathVariable String id,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.approveOrder(id, userId, token));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ADMIN')")
    @Operation(summary = "Reject a pending order")
    public ResponseEntity<Order> reject(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.rejectOrder(id, body.get("reason"), userId));
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR', 'ADMIN')")
    @Operation(summary = "Mark order as dispatched")
    public ResponseEntity<Order> dispatch(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.dispatchOrder(id, body.get("trackingNumber"), userId));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasRole('HOSPITAL')")
    @Operation(summary = "Confirm delivery of an order")
    public ResponseEntity<Order> confirmDelivery(
            @PathVariable String id,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.confirmDelivery(id, userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<Order> cancel(
            @PathVariable String id,
            HttpServletRequest httpRequest
    ) {
        String token = extractToken(httpRequest);
        String userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, token));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : "";
    }
}
