package com.example.IRMS.modules.digital_ordering.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRMS.modules.digital_ordering.dtos.CreateOrderRequest;
import com.example.IRMS.modules.digital_ordering.dtos.UpdateOrderRequest;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.digital_ordering.services.OrderProcessingService;
import com.example.IRMS.modules.kitchen_coordination.dtos.OrderResponseDto;
import com.example.IRMS.modules.kitchen_coordination.services.OrderTrackingService;
import com.example.IRMS.utils.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderProcessingService orderProcessingService;
	private final OrderTrackingService orderTrackingService;

	@Operation(summary = "Get order by ID", description = "Returns full order details for the provided order ID")
	@GetMapping("get/{id}")
	@PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_VIEW_KDS')")
	public ResponseEntity<ApiResponse<OrderEntity>> getOrderById(@PathVariable Long id) {
		OrderEntity order = orderProcessingService.getOrderById(id);
		return ResponseEntity.ok(new ApiResponse<>(200, "Order fetched successfully", order));
	}

	@Operation(summary = "Get all orders", description = "Returns all orders with their items and status for dashboard tracking")
	@GetMapping("/all")
	@PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_VIEW_KDS')")
	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders() {
		List<OrderResponseDto> orders = orderProcessingService.getAllOrders().stream()
				.map(OrderResponseDto::fromEntity)
				.toList();
		return ResponseEntity.ok(new ApiResponse<>(200, "Orders fetched successfully", orders));
	}

	@Operation(summary = "Create order", description = "Creates a new order with initial items and sets status to pending")
	@PostMapping("/create")
	@PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER')")
	public ResponseEntity<ApiResponse<OrderEntity>> createOrder(
			@Valid @RequestBody CreateOrderRequest request,
			@AuthenticationPrincipal String userId) {
		OrderEntity createdOrder = orderProcessingService.createOrder(request, userId);
		return ResponseEntity.ok(new ApiResponse<>(201, "Order created successfully", createdOrder));
	}

	@Operation(summary = "Update pending order", description = "Updates table, staff, or items for an order only when the order is still pending")
	@PostMapping("/update/{id}")
	@PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER')")
	public ResponseEntity<ApiResponse<OrderEntity>> updateOrder(
			@PathVariable Long id,
			@Valid @RequestBody UpdateOrderRequest request) {
		OrderEntity updatedOrder = orderProcessingService.updatePendingOrder(id, request);
		return ResponseEntity.ok(new ApiResponse<>(200, "Order updated successfully", updatedOrder));
	}

	@Operation(summary = "Cancel pending order", description = "Cancels an order if and only if its current status is pending")
	@PostMapping("/cancel/{id}")
	@PreAuthorize("hasAnyAuthority('PERM_CANCEL_ORDER')")
	public ResponseEntity<ApiResponse<OrderEntity>> cancelOrder(@PathVariable Long id) {
		OrderEntity order = orderProcessingService.getOrderById(id);
		if (order.getStatus() == OrderStatus.PENDING) {
			OrderEntity updatedOrder = orderTrackingService.markOrderCanceled(id);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order canceled successfully", updatedOrder));
		} else {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, "Only pending orders can be canceled", null));
		}
	}

	@Operation(summary = "Complete order", description = "Marks a READY order as COMPLETED when customers are done eating")
	@PatchMapping("/{id}/complete")
	@PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_VIEW_KDS')")
	public ResponseEntity<ApiResponse<OrderEntity>> completeOrder(@PathVariable Long id) {
		OrderEntity order = orderProcessingService.getOrderById(id);
		if (order.getStatus() == OrderStatus.READY) {
			order.setStatus(OrderStatus.COMPLETED);
			order.setCompletedAt(LocalDateTime.now());
			if (order.getOrderTime() != null) {
				long actualMinutes = Duration.between(order.getOrderTime(), order.getCompletedAt()).toMinutes();
				order.setActualPrepMinutes((int) Math.max(actualMinutes, 0));
			}
			OrderEntity updatedOrder = orderProcessingService.updateOrder(order);
			orderTrackingService.notifyOrderCompletion(updatedOrder);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order completed successfully", updatedOrder));
		} else {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, "Only READY orders can be completed", null));
		}
	}

}
