package com.example.IRMS.modules.kitchen_coordination.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.kitchen_coordination.dtos.KdsAlertDto;
import com.example.IRMS.modules.kitchen_coordination.dtos.OrderResponseDto;
import com.example.IRMS.modules.kitchen_coordination.enums.OrderSortBy;
import com.example.IRMS.modules.kitchen_coordination.enums.SortDirection;
import com.example.IRMS.modules.kitchen_coordination.facades.KDSFacade;
import com.example.IRMS.utils.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/kds")
@RequiredArgsConstructor
public class KDSController {
	private final KDSFacade kdsFacade;

	@Operation(summary = "Get KDS order queue", description = "Returns active orders in the kitchen queue with all details including items")
	@GetMapping("/queue")
	@PreAuthorize("hasAnyAuthority('PERM_VIEW_KDS')")
	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getQueue( 
			@RequestParam(defaultValue = "ORDER_TIME") OrderSortBy sortBy,
			@RequestParam(defaultValue = "DESC") SortDirection direction) {
		List<OrderEntity> orders = kdsFacade.getQueue(sortBy, direction);
		List<OrderResponseDto> dtos = orders.stream()
			.map(OrderResponseDto::fromEntity)
			.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse<>(200, "KDS order queue fetched", dtos));
	}

	@Operation(summary = "Get KDS deadline alerts", description = "Returns order-level alerts for orders that are approaching their computed deadline")
	@GetMapping("/alerts")
	@PreAuthorize("hasAnyAuthority('PERM_VIEW_KDS')")
	public ResponseEntity<ApiResponse<List<KdsAlertDto>>> getAlerts(
			@RequestParam(defaultValue = "2") int thresholdMinutes) {
		return ResponseEntity.ok(new ApiResponse<>(200, "KDS alerts fetched", kdsFacade.getAlerts(thresholdMinutes)));
	}

	@Operation(summary = "Start an order item", description = "Marks a dish as cooking. Required stations are read from MenuItemEntity for display only")
	@PatchMapping("/orders/{orderId}/items/{itemId}/start")
	@PreAuthorize("hasAnyAuthority('PERM_UPDATE_ORDER_PROGRESS')")
	public ResponseEntity<ApiResponse<OrderEntity>> startItem(
			@PathVariable Long orderId,
			@PathVariable Long itemId) {
		try {
			OrderEntity updated = kdsFacade.startItem(orderId, itemId);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order item started", updated));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
		}
	}

	@Operation(summary = "Mark order cooking", description = "Marks the whole order as cooking when at least one item is cooking. For display only, does not affect item-level status")
	@PatchMapping("/orders/{orderId}/mark-cooking")
	@PreAuthorize("hasAnyAuthority('PERM_UPDATE_ORDER_PROGRESS')")
	public ResponseEntity<ApiResponse<OrderEntity>> markOrderCooking(@PathVariable Long orderId) {
		try {
			OrderEntity updated = kdsFacade.markOrderCooking(orderId);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order marked cooking", updated));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
		}
	}

	@Operation(summary = "Mark item ready", description = "Marks one dish as ready to serve")
	@PatchMapping("/orders/{orderId}/items/{itemId}/ready")
	@PreAuthorize("hasAnyAuthority('PERM_UPDATE_ORDER_PROGRESS')")
	public ResponseEntity<ApiResponse<OrderEntity>> markItemReady(
			@PathVariable Long orderId,
			@PathVariable Long itemId) {
		try {
			OrderEntity updated = kdsFacade.markItemReady(orderId, itemId);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order item marked ready", updated));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
		}
	}

	@Operation(summary = "Cancel an order item", description = "Marks one dish as canceled")
	@PatchMapping("/orders/{orderId}/items/{itemId}/cancel")
	@PreAuthorize("hasAnyAuthority('PERM_UPDATE_ORDER_PROGRESS')")
	public ResponseEntity<ApiResponse<OrderEntity>> cancelItem(
			@PathVariable Long orderId,
			@PathVariable Long itemId) {
		try {
			OrderEntity updated = kdsFacade.cancelItem(orderId, itemId);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order item canceled", updated));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
		}
	}

	@Operation(summary = "Complete an order item", description = "Marks one dish as done. When all dishes are done, the order is marked completed")
	@PatchMapping("/orders/{orderId}/items/{itemId}/complete")
	@PreAuthorize("hasAnyAuthority('PERM_UPDATE_ORDER_PROGRESS')")
	public ResponseEntity<ApiResponse<OrderEntity>> completeItem(
			@PathVariable Long orderId,
			@PathVariable Long itemId) {
		try {
			OrderEntity updated = kdsFacade.completeItem(orderId, itemId);
			return ResponseEntity.ok(new ApiResponse<>(200, "Order item completed", updated));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
		}
	}
}
