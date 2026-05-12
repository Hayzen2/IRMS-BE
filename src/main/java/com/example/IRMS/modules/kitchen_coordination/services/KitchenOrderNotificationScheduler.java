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

	private boolean isNearDeadline(OrderEntity order, LocalDateTime now, int thresholdMinutes) {
		LocalDateTime orderTime = order.getOrderTime();
		if (orderTime == null) {
			return false;
		}
		LocalDateTime deadline = orderTime.plusMinutes(getOrderEstimatedPrep(order));
		long remaining = Duration.between(now, deadline).toMinutes();
		return remaining >= 0 && remaining <= thresholdMinutes;
	}

	private boolean isOverdue(OrderEntity order, LocalDateTime now) {
		LocalDateTime orderTime = order.getOrderTime();
		if (orderTime == null) {
			return false;
		}
		LocalDateTime deadline = orderTime.plusMinutes(getOrderEstimatedPrep(order));
		return now.isAfter(deadline);
	}

	private int getOrderEstimatedPrep(OrderEntity order) {
		int total = 0;
		for (OrderItemEntity item : order.getItems()) {
			if (item.getProgressStatus() == OrderItemProgressStatus.CANCELED) {
				continue;
			}
			if (item.getMenuItem() == null || item.getMenuItem().getEstimatedPrepMinutes() == null) {
				continue;
			}
			total += item.getMenuItem().getEstimatedPrepMinutes() * Math.max(item.getQuantity(), 1);
		}
		return total;
	}
}