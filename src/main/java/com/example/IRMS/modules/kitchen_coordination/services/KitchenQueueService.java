package com.example.IRMS.modules.kitchen_coordination.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.enums.OrderItemProgressStatus;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.enums.StationType;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderItemEntity;
import com.example.IRMS.modules.digital_ordering.repositories.OrderRepository;
import com.example.IRMS.modules.kitchen_coordination.dtos.KdsAlertDto;
import com.example.IRMS.modules.kitchen_coordination.dtos.KdsQueueItemDto;
import com.example.IRMS.modules.kitchen_coordination.enums.OrderSortBy;
import com.example.IRMS.modules.kitchen_coordination.enums.SortDirection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KitchenQueueService {
	private final OrderRepository orderRepository;

	// get order queue with flexible sorting - returns full OrderEntity objects with all details
	public List<OrderEntity> getOrderQueue(OrderSortBy sortBy, SortDirection direction, boolean includeHistory) {
		List<OrderEntity> queue = new ArrayList<>();
		
		for (OrderEntity order : orderRepository.findAll()) {
			// Only filter out finished orders if includeHistory is FALSE (i.e., the Chef view)
			if (!includeHistory && (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED)) {
				continue;
			}
			queue.add(order);
		}

		// apply custom sorting based on sortBy parameter
		Comparator<OrderEntity> comparator = switch (sortBy) {
			case ORDER_TIME -> Comparator.comparing(OrderEntity::getOrderTime);
			case ESTIMATED_PREP_TIME -> Comparator.comparing(
				(OrderEntity order) -> getOrderEstimatedPrep(order),
				Comparator.nullsLast(Integer::compareTo));
		};

		if (direction == SortDirection.DESC) {
			comparator = comparator.reversed();
		}

		queue.sort(comparator);
		return queue;
	}

	// get alerts for orders nearing their deadline or overdue
	public List<KdsAlertDto> getAlerts(int thresholdMinutes) {
		LocalDateTime now = LocalDateTime.now();
		List<KdsAlertDto> alerts = new ArrayList<>();

		for (OrderEntity order : orderRepository.findAll()) {
			if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.COOKING) {
				continue;
			}

			for (OrderItemEntity item : order.getItems()) {
				if (!isQueueableItem(item)) continue;

				Long remaining = calculateRemainingMinutes(item, order.getOrderTime(), now);

				if (remaining != null && remaining <= thresholdMinutes) {
					String alertType = remaining < 0 ? "ITEM_OVERDUE" : "ITEM_NEAR_DEADLINE";
					String alertMsg = remaining < 0 
							? "Item '" + item.getMenuItem().getName() + "' is OVERDUE!"
							: "Item '" + item.getMenuItem().getName() + "' (x" + item.getQuantity() + ") is nearing deadline";

					alerts.add(KdsAlertDto.builder()
							.type(alertType)
							.message(alertMsg)
							.orderId(order.getId())
							.orderItemId(item.getId())
							.createdAt(now)
							.build());
				}
			}
		}

		return alerts;
	}

	// convert OrderEntity to KdsQueueItemDto for KDS display
	private KdsQueueItemDto toQueueDto(OrderEntity order, LocalDateTime now) {
		int estimatedPrep = getOrderEstimatedPrep(order);
		LocalDateTime start = order.getOrderTime() == null ? now : order.getOrderTime();
		LocalDateTime deadline = start.plusMinutes(estimatedPrep);
		DishCategory primaryDishCategory = null;
		StationType primaryStation = StationType.GENERAL;

		for (OrderItemEntity item : order.getItems()) {
			if (!isQueueableItem(item)) {
				continue;
			}

			if (primaryDishCategory == null && item.getMenuItem() != null) {
				primaryDishCategory = item.getMenuItem().getDishCategory();
			}

			if (item.getMenuItem() != null
					&& item.getMenuItem().getStations() != null
					&& !item.getMenuItem().getStations().isEmpty()) {
				primaryStation = item.getMenuItem().getStations().get(0);
			}

			break;
		}

		return KdsQueueItemDto.builder()
				.orderId(order.getId())
				.orderTime(start)
				.deadline(deadline)
				.estimatedPrepMinutes(estimatedPrep)
				.actualPrepMinutes(order.getActualPrepMinutes())
				.primaryDishCategory(primaryDishCategory)
				.primaryStation(primaryStation)
				.status(order.getStatus())
				.nearDeadline(isNearDeadline(order, now, 2))
				.build();
	}

	// calculate the estimated preparation time for an order
	private Integer getOrderEstimatedPrep(OrderEntity order) {
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

	// check if an order item should be included in the KDS queue (not canceled or completed)
	private boolean isQueueableItem(OrderItemEntity item) {
		return item.getProgressStatus() != OrderItemProgressStatus.CANCELED
				&& item.getProgressStatus() != OrderItemProgressStatus.COMPLETED;
	}

	// check if an order is near its deadline or overdue
	private boolean isNearDeadline(OrderEntity order, LocalDateTime now, int thresholdMinutes) {
		for (OrderItemEntity item : order.getItems()) {
			if (!isQueueableItem(item)) {
				continue;
			}
			
			Long remaining = calculateRemainingMinutes(item, order.getOrderTime(), now);
			
			if (remaining != null && remaining <= thresholdMinutes) {
				return true;
			}
		}
		return false;
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
}