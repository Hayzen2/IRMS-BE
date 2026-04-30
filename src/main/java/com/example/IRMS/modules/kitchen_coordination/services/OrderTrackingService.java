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
		messagingTemplate.convertAndSend("/topic/kds/new-order", order);
	}

	// notify KDS of order status update for real-time update
	public void notifyOrderCompletion(OrderEntity order) {
		messagingTemplate.convertAndSend("/topic/kds/order-completed", order);
	}

	// notify KDS of order near deadline for real-time update
	public void notifyOrderNearDeadline(OrderEntity order) {
		messagingTemplate.convertAndSend("/topic/kds/order-near-deadline", order);
	}

	// notify KDS of order cancellation for real-time update
	public void notifyOrderCanceled(OrderEntity order) {
		messagingTemplate.convertAndSend("/topic/kds/order-canceled", order);
	}

	// cancel an entire order
	@Transactional
	public OrderEntity markOrderCanceled(Long orderId) {
		OrderEntity order = findOrder(orderId);
		order.setStatus(OrderStatus.CANCELED);
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() != OrderItemProgressStatus.COMPLETED) {
				item.setProgressStatus(OrderItemProgressStatus.CANCELED);
			}
		}
		OrderEntity saved = orderRepository.save(order);
		notifyOrderCanceled(saved);
		return saved;
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
		return orderRepository.save(order);
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
		order.setStatus(OrderStatus.COOKING);
		return orderRepository.save(order);
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
			notifyOrderCanceled(order);
		}
		return orderRepository.save(order);
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
