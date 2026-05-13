package com.example.IRMS.modules.kitchen_coordination.services;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.IRMS.modules.digital_ordering.enums.OrderItemProgressStatus;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderItemEntity;
import com.example.IRMS.modules.digital_ordering.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenOrderNotificationScheduler {

	private final OrderRepository orderRepository;
	private final OrderTrackingService orderTrackingService;

	// Runs every minute to check for orders approaching or past their estimated prep time and publishes alerts
	// @Scheduled(fixedDelay = 60000) means it runs automatically every 60 seconds after the previous execution finishes
	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void publishKitchenAlerts() {
		LocalDateTime now = LocalDateTime.now();
		for (OrderEntity order : orderRepository.findAll()) {
			if (!hasActiveItems(order)) {
				continue;
			}

			if (isOverdue(order, now)) {
				if (!order.isOverdueNotified()) {
					order.setOverdueNotified(true);
					orderRepository.save(order);
					orderTrackingService.notifyOrderOverdue(order);
					log.info("Published overdue alert for order {}", order.getId());
				}
				continue;
			}

			if (isNearDeadline(order, now, 2) && !order.isNearDeadlineNotified()) {
				order.setNearDeadlineNotified(true);
				orderRepository.save(order);
				orderTrackingService.notifyOrderNearDeadline(order);
				log.info("Published near-deadline alert for order {}", order.getId());
			}
		}
	}

	private boolean hasActiveItems(OrderEntity order) {
		if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
			return false;
		}
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() != OrderItemProgressStatus.READY
					&& item.getProgressStatus() != OrderItemProgressStatus.COMPLETED
					&& item.getProgressStatus() != OrderItemProgressStatus.CANCELED) {
				return true;
			}
		}
		return false;
	}

	private boolean isOverdue(OrderEntity order, LocalDateTime now) {
		for (OrderItemEntity item : order.getItems()) {
			if (!isQueueableItem(item)) continue;

			Long remaining = calculateRemainingMinutes(item, order.getOrderTime(), now);
			
			// If remaining is strictly less than 0, the item is overdue
			if (remaining != null && remaining < 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isNearDeadline(OrderEntity order, LocalDateTime now, int thresholdMinutes) {
		for (OrderItemEntity item : order.getItems()) {
			if (!isQueueableItem(item)) continue;
			
			Long remaining = calculateRemainingMinutes(item, order.getOrderTime(), now);
			
			// We check >= 0 here because isOverdue (above) handles the < 0 cases
			if (remaining != null && remaining >= 0 && remaining <= thresholdMinutes) {
				return true;
			}
		}
		return false;
	}

	// check if an order item should be included in the time calculations
	private boolean isQueueableItem(OrderItemEntity item) {
		return item.getProgressStatus() != OrderItemProgressStatus.CANCELED
				&& item.getProgressStatus() != OrderItemProgressStatus.COMPLETED;
	}

	// helper method to calculate remaining minutes for an item
	private Long calculateRemainingMinutes(OrderItemEntity item, LocalDateTime orderTime, LocalDateTime now) {
		if (orderTime == null || item.getMenuItem() == null || item.getMenuItem().getEstimatedPrepMinutes() == null) {
			return null;
		}
		
		int totalItemPrepTime = item.getMenuItem().getEstimatedPrepMinutes() * Math.max(item.getQuantity(), 1);
		LocalDateTime itemDeadline = orderTime.plusMinutes(totalItemPrepTime);
		
		return Duration.between(now, itemDeadline).toMinutes();
	}

	private int getOrderEstimatedPrep(OrderEntity order) {
		int total = 0;
		for (OrderItemEntity item : order.getItems()) {
			if (!isQueueableItem(item)) continue;
			if (item.getMenuItem() == null || item.getMenuItem().getEstimatedPrepMinutes() == null) continue;
			
			total += item.getMenuItem().getEstimatedPrepMinutes() * Math.max(item.getQuantity(), 1);
		}
		return total;
	}
}