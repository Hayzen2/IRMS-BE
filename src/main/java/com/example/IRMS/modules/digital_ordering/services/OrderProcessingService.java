package com.example.IRMS.modules.digital_ordering.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.IRMS.config.exceptions.BadRequestException;
import com.example.IRMS.config.exceptions.ConflictException;
import com.example.IRMS.config.exceptions.ResourceNotFoundException;
import com.example.IRMS.modules.digital_ordering.dtos.CreateOrderItemRequest;
import com.example.IRMS.modules.digital_ordering.dtos.CreateOrderRequest;
import com.example.IRMS.modules.digital_ordering.dtos.UpdateOrderItemRequest;
import com.example.IRMS.modules.digital_ordering.dtos.UpdateOrderRequest;
import com.example.IRMS.modules.digital_ordering.enums.OrderItemProgressStatus;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderItemEntity;
import com.example.IRMS.modules.admin_tools.repositories.UserRepository;
import com.example.IRMS.modules.digital_ordering.repositories.MenuRepository;
import com.example.IRMS.modules.digital_ordering.repositories.OrderRepository;
import com.example.IRMS.modules.kitchen_coordination.services.OrderTrackingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {
	private final OrderRepository orderRepository;
	private final MenuRepository menuRepository;
	private final UserRepository userRepository;
	private final OrderTrackingService orderTrackingService;

	public OrderEntity getOrderById(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
	}

	public List<OrderEntity> getAllOrders() {
		return orderRepository.findAll();
	}

	@Transactional
	public OrderEntity createOrder(CreateOrderRequest request, String userId) {
		OrderEntity order = new OrderEntity();

		Long authenticatedStaffId;
		try {
			authenticatedStaffId = Long.valueOf(userId);
		} catch (NumberFormatException ex) {
			throw new BadRequestException("Invalid authenticated user ID", ex);
		}

		UserEntity staff = userRepository.findById(authenticatedStaffId)
				.orElseThrow(() -> new ResourceNotFoundException("Authenticated staff not found"));
		order.setStaff(staff);
		
		order.setOrderTime(LocalDateTime.now());
		order.setStatus(OrderStatus.PENDING);
    order.setTableId(request.getTableId());
		List<OrderItemEntity> orderItems = new ArrayList<>();
		for (CreateOrderItemRequest itemRequest : request.getItems()) {
			MenuItemEntity menuItem = menuRepository.findById(itemRequest.getMenuItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
			OrderItemEntity orderItem = new OrderItemEntity();
			orderItem.setMenuItem(menuItem);
			orderItem.setQuantity(itemRequest.getQuantity());
			orderItem.setOrder(order);
			orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
			orderItem.setAllergyNotes(itemRequest.getAllergyNotes());
			orderItem.setCustomization(itemRequest.getCustomization());
			orderItem.setProgressStatus(OrderItemProgressStatus.PENDING);
			orderItems.add(orderItem);
		}
		order.setItems(orderItems);
		OrderEntity savedOrder = orderRepository.save(order);
		orderTrackingService.notifyNewOrder(savedOrder);
		return savedOrder;
	}

	public OrderEntity updateOrder(OrderEntity order) {
		return orderRepository.save(order);
	}

	// transactional to ensure data integrity during order update (all-or-nothing)
	// only pending orders can be updated
	@Transactional
	public OrderEntity updatePendingOrder(Long id, UpdateOrderRequest request) {
		OrderEntity order = getOrderById(id);

		if (order.getStatus() != OrderStatus.PENDING) {
			throw new ConflictException("Only pending orders can be edited");
		}

		if (request.getTableId() != null) {
			order.setTableId(request.getTableId());
		}

		if (request.getStaffId() != null) {
			UserEntity staff = userRepository.findById(request.getStaffId())
					.orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
			order.setStaff(staff);
		}

		if (request.getItems() != null) {
			Map<Long, OrderItemEntity> existingItemsById = new HashMap<>();

			for (OrderItemEntity item : order.getItems()) {
				existingItemsById.put(item.getId(), item);
			}

			for (UpdateOrderItemRequest itemRequest : request.getItems()) {
				if (itemRequest.getItemId() == null) {
					addNewOrderItem(order, itemRequest);
					continue;
				}

				OrderItemEntity existingItem = existingItemsById.get(itemRequest.getItemId());
				if (existingItem == null) {
					throw new ResourceNotFoundException("Order item not found for this order");
				}

				if (itemRequest.isCanceled()) {
					existingItem.setProgressStatus(OrderItemProgressStatus.CANCELED);
					continue;
				}

				updateExistingItem(existingItem, itemRequest);
			}
		}

		return orderRepository.save(order);
	}

	// Helper methods for adding new order items and updating existing items in updatePendingOrder
	private void addNewOrderItem(OrderEntity order, UpdateOrderItemRequest itemRequest) {
		if (itemRequest.getMenuItemId() == null) {
			throw new BadRequestException("menuItemId is required when adding a new order item");
		}

		if (itemRequest.getQuantity() <= 0) {
			throw new BadRequestException("quantity must be at least 1");
		}

		MenuItemEntity menuItem = menuRepository.findById(itemRequest.getMenuItemId())
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

		OrderItemEntity newItem = new OrderItemEntity();
		newItem.setOrder(order);
		newItem.setMenuItem(menuItem);
		newItem.setQuantity(itemRequest.getQuantity());
		newItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
		newItem.setAllergyNotes(itemRequest.getAllergyNotes());
		newItem.setCustomization(itemRequest.getCustomization());
		newItem.setProgressStatus(OrderItemProgressStatus.PENDING);

		order.getItems().add(newItem);
	}

	// Helper method to update existing order item based on UpdateOrderItemRequest
	private void updateExistingItem(OrderItemEntity existingItem, UpdateOrderItemRequest itemRequest) {
		if (itemRequest.getQuantity() > 0) {
			existingItem.setQuantity(itemRequest.getQuantity());
		}

		if (itemRequest.getMenuItemId() != null) {
			MenuItemEntity menuItem = menuRepository.findById(itemRequest.getMenuItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
			existingItem.setMenuItem(menuItem);
		}

		existingItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
		existingItem.setAllergyNotes(itemRequest.getAllergyNotes());
		existingItem.setCustomization(itemRequest.getCustomization());
	}


}
