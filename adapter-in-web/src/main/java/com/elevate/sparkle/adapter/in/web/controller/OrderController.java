package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.ApiResponse;
import com.elevate.sparkle.adapter.in.web.dto.CreateOrderRequest;
import com.elevate.sparkle.adapter.in.web.dto.OrderResponse;
import com.elevate.sparkle.adapter.in.web.dto.UpdateOrderStatusRequest;
import com.elevate.sparkle.adapter.in.web.mapper.OrderMapper;
import com.elevate.sparkle.application.port.in.*;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import com.elevate.sparkle.domain.valueobject.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Order operations
 * Thin adapter - delegates to use cases
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final OrderMapper orderMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("REST: Creating order for user: {}", request.getUserId());
        
        Order order = createOrderUseCase.createOrder(orderMapper.toCommand(request));
        OrderResponse response = orderMapper.toResponse(order);
        
        return ApiResponse.success(response, HttpStatus.CREATED.value());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("REST: Fetching order: {}", orderId);
        
        Order order = getOrderUseCase.getOrder(OrderId.of(orderId));
        OrderResponse response = orderMapper.toResponse(order);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List orders by user ID")
    public ApiResponse<List<OrderResponse>> listOrdersByUser(@PathVariable String userId) {
        log.info("REST: Listing orders for user: {}", userId);
        
        List<Order> orders = listOrdersUseCase.listOrdersByUser(UserId.of(userId));
        List<OrderResponse> responses = orderMapper.toResponses(orders);
        
        return ApiResponse.success(responses);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "List orders by status")
    public ApiResponse<List<OrderResponse>> listOrdersByStatus(@PathVariable String status) {
        log.info("REST: Listing orders with status: {}", status);
        
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = listOrdersUseCase.listOrdersByStatus(orderStatus);
        List<OrderResponse> responses = orderMapper.toResponses(orders);
        
        return ApiResponse.success(responses);
    }

    @GetMapping
    @Operation(summary = "List all orders with pagination")
    public ApiResponse<List<OrderResponse>> listAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST: Listing all orders - page: {}, size: {}", page, size);
        
        List<Order> orders = listOrdersUseCase.listAllOrders(page, size);
        List<OrderResponse> responses = orderMapper.toResponses(orders);
        
        return ApiResponse.success(responses);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("REST: Updating order {} status to {}", orderId, request.getStatus());
        
        UpdateOrderStatusUseCase.UpdateStatusCommand command = 
                new UpdateOrderStatusUseCase.UpdateStatusCommand(
                        OrderId.of(orderId),
                        OrderStatus.valueOf(request.getStatus().toUpperCase()),
                        request.getReason()
                );
        
        Order order = updateOrderStatusUseCase.updateStatus(command);
        OrderResponse response = orderMapper.toResponse(order);
        
        return ApiResponse.success(response);
    }
}
