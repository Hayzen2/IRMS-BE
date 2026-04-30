package com.example.IRMS.modules.kitchen_coordination.dtos;

import java.time.LocalDateTime;

import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.enums.StationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KdsQueueItemDto {
    private Long orderId;
    private LocalDateTime orderTime;
    private LocalDateTime deadline;
    private Integer estimatedPrepMinutes;
    private Integer actualPrepMinutes;
    private DishCategory primaryDishCategory;
    private StationType primaryStation;
    private OrderStatus status;
    private boolean nearDeadline;
}
