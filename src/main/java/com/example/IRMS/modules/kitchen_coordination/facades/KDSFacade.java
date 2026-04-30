package com.example.IRMS.modules.kitchen_coordination.facades;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.example.IRMS.modules.kitchen_coordination.dtos.KdsAlertDto;
import com.example.IRMS.modules.kitchen_coordination.dtos.KdsQueueItemDto;
import com.example.IRMS.modules.kitchen_coordination.enums.OrderSortBy;
import com.example.IRMS.modules.kitchen_coordination.enums.SortDirection;
import com.example.IRMS.modules.kitchen_coordination.services.KitchenQueueService;
import com.example.IRMS.modules.kitchen_coordination.services.OrderTrackingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KDSFacade {
    private final KitchenQueueService kitchenQueueService;
    private final OrderTrackingService orderTrackingService;

    // get queue with custom sorting
    public List<KdsQueueItemDto> getQueue(OrderSortBy sortBy, SortDirection direction) {
        return kitchenQueueService.getQueue(sortBy, direction);
    }

    public List<KdsAlertDto> getAlerts(int thresholdMinutes) {
        return kitchenQueueService.getAlerts(thresholdMinutes);
    }

    public OrderEntity startItem(Long orderId, Long itemId) {
        return orderTrackingService.startItem(orderId, itemId);
    }

    public OrderEntity markItemReady(Long orderId, Long itemId) {
        return orderTrackingService.markItemReady(orderId, itemId);
    }

    public OrderEntity cancelItem(Long orderId, Long itemId) {
        return orderTrackingService.markItemCanceled(orderId, itemId);
    }

    public OrderEntity completeItem(Long orderId, Long itemId) {
        return orderTrackingService.completeItem(orderId, itemId);
    }
}
