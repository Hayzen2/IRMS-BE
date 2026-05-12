package com.example.IRMS.modules.kitchen_coordination.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.IRMS.config.exceptions.ResourceNotFoundException;
import com.example.IRMS.modules.digital_ordering.enums.OrderItemProgressStatus;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderItemEntity;
import com.example.IRMS.modules.digital_ordering.repositories.OrderRepository;
import com.example.IRMS.modules.kitchen_coordination.dtos.OrderResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {
	private final OrderRepository orderRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// get current status of an order by ID
	public OrderStatus getOrderStatus(Long orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		return order.getStatus();
	}

	// notify KDS of new order for real-time update
	public void notifyNewOrder(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/new-order", payload);
		messagingTemplate.convertAndSend("/topic/orders/new-order", payload);
	}

	// notify KDS of order status update for real-time update
	public void notifyOrderReady(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-ready", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-ready", payload);
	}

	// notify KDS of order status cooking for real-time update
	public void notifyOrderCooking(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-cooking", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-cooking", payload);
	}

	// notify KDS of any order update (useful for per-item ready/cancel updates)
	public void notifyOrderUpdated(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-updated", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-updated", payload);
	}

	// notify KDS of order status update for real-time update
	public void notifyOrderCompletion(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-completed", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-completed", payload);
	}

	// notify KDS of order near deadline for real-time update
	public void notifyOrderNearDeadline(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-near-deadline", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-near-deadline", payload);
	}

	// notify KDS of order overdue for real-time update
	public void notifyOrderOverdue(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-overdue", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-overdue", payload);
	}

	// notify KDS of order cancellation for real-time update
	public void notifyOrderCanceled(OrderEntity order) {
		OrderResponseDto payload = OrderResponseDto.fromEntity(order);
		messagingTemplate.convertAndSend("/topic/kds/order-canceled", payload);
		messagingTemplate.convertAndSend("/topic/orders/order-canceled", payload);
	}

	// notify KDS of order started (in progress) for real-time update
	public void notifyOrderStarted(OrderEntity order) {
		messagingTemplate.convertAndSend("/topic/kds/order-started", order);
	}

	// cancel an entire order
	@Transactional
	public OrderEntity markOrderCanceled(Long orderId) {
		OrderEntity order = findOrder(orderId);
		order.setStatus(OrderStatus.CANCELED);
		order.setCompletedAt(LocalDateTime.now());
		if (order.getOrderTime() != null) {
			long actualMinutes = Duration.between(order.getOrderTime(), order.getCompletedAt()).toMinutes();
			order.setActualPrepMinutes((int) Math.max(actualMinutes, 0));
		}
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() != OrderItemProgressStatus.COMPLETED) {
				item.setProgressStatus(OrderItemProgressStatus.CANCELED);
			}
		}
		OrderEntity saved = orderRepository.save(order);
		notifyOrderCanceled(saved);
		return saved;
	}

	@Transactional
	public OrderEntity markOrderCooking(Long orderId) {
		OrderEntity order = findOrder(orderId);
		if (order.getStatus() == OrderStatus.PENDING) {
			order.setStatus(OrderStatus.COOKING);
			OrderEntity saved = orderRepository.save(order);
			notifyOrderCooking(saved);
			return saved;
		} else {
			throw new IllegalStateException("Only pending orders can be marked cooking");
		}
	}

	// start making an item, update order status to COOKING
	@Transactional
	public OrderEntity startItem(Long orderId, Long itemId) {
		return markItemCooking(orderId, itemId);
	}

	// mark an item as cooking
	@Transactional
	public OrderEntity markItemCooking(Long orderId, Long itemId) {
		OrderEntity order = findOrder(orderId);
		OrderItemEntity item = findItemInOrder(order, itemId);

		if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED
				|| item.getProgressStatus() == OrderItemProgressStatus.COMPLETED) {
			throw new IllegalStateException("This item cannot be started");
		}

		item.setProgressStatus(OrderItemProgressStatus.COOKING);
		order.setStatus(OrderStatus.COOKING);
		OrderEntity saved = orderRepository.save(order);
		// notify that order has entered cooking for UI update using persisted state
		notifyOrderCooking(saved);
		return saved;
	}

	// mark an item as ready
	@Transactional
	public OrderEntity markItemReady(Long orderId, Long itemId) {
		OrderEntity order = findOrder(orderId);
		OrderItemEntity item = findItemInOrder(order, itemId);

		if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED
				|| item.getProgressStatus() == OrderItemProgressStatus.COMPLETED) {
			throw new IllegalStateException("This item cannot be marked ready");
		}

		item.setProgressStatus(OrderItemProgressStatus.READY);
		// always surface an order-updated event so clients can refresh per-item state
		order.setStatus(OrderStatus.COOKING);
		if (allItemsReady(order)) {
			order.setStatus(OrderStatus.READY);
		}
		OrderEntity saved = orderRepository.save(order);
		// notify clients about the per-item update first
		notifyOrderUpdated(saved);
		if (saved.getStatus() == OrderStatus.READY) {
			// send full-ready notification as well
			notifyOrderReady(saved);
		}
		return saved;
	}

	// mark an item as canceled
	@Transactional
	public OrderEntity markItemCanceled(Long orderId, Long itemId) {
		OrderEntity order = findOrder(orderId);
		OrderItemEntity item = findItemInOrder(order, itemId);

		if (item.getProgressStatus() == OrderItemProgressStatus.COMPLETED) {
			throw new IllegalStateException("Completed items cannot be canceled");
		}

		item.setProgressStatus(OrderItemProgressStatus.CANCELED);
		if (allItemsCanceled(order)) {
			order.setStatus(OrderStatus.CANCELED);
		}
		OrderEntity saved = orderRepository.save(order);
		if (saved.getStatus() == OrderStatus.CANCELED) {
			notifyOrderCanceled(saved);
		}
		return saved;
	}

	// complete an item, update order status to COMPLETED if all items are done
	@Transactional
	public OrderEntity completeItem(Long orderId, Long itemId) {
		OrderEntity order = findOrder(orderId);
		OrderItemEntity item = findItemInOrder(order, itemId);

		if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED) {
			throw new IllegalStateException("Canceled items cannot be completed");
		}

		item.setProgressStatus(OrderItemProgressStatus.COMPLETED);
		item.setCompletedAt(LocalDateTime.now());
		if (allItemsCompleted(order)) {
			order.setStatus(OrderStatus.COMPLETED);
			if (order.getOrderTime() != null) {
				if (order.getCompletedAt() == null) {
					order.setCompletedAt(LocalDateTime.now());
				}
				long actualMinutes = Duration.between(order.getOrderTime(), order.getCompletedAt()).toMinutes();
				order.setActualPrepMinutes((int) Math.max(actualMinutes, 0));
			}
		}

		OrderEntity saved = orderRepository.save(order);
		if (saved.getStatus() == OrderStatus.COMPLETED) {
			notifyOrderCompletion(saved);
		}
		return saved;
	}

	// get order by ID
	private OrderEntity findOrder(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
	}

	// get item in an order by item ID
	private OrderItemEntity findItemInOrder(OrderEntity order, Long itemId) {
		for (OrderItemEntity item : order.getItems()) {
			if (Objects.equals(item.getId(), itemId)) {
				return item;
			}
		}
		throw new ResourceNotFoundException("Order item not found for this order");
	}

	// check if all items in an order are completed (ignoring canceled items)
	private boolean allItemsCompleted(OrderEntity order) {
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED) {
				continue;
			}
			if (item.getProgressStatus() != OrderItemProgressStatus.COMPLETED) {
				return false;
			}
		}
		return true;
	}

	// check if all items in an order are ready (ignoring canceled items)
	private boolean allItemsReady(OrderEntity order) {
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED) {
				continue;
			}
			if (item.getProgressStatus() != OrderItemProgressStatus.READY) {
				return false;
			}
		}
		return true;
	}

	// check if all items in an order are canceled
	private boolean allItemsCanceled(OrderEntity order) {
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() != OrderItemProgressStatus.CANCELED) {
				return false;
			}
		}
		return true;
	}

}
