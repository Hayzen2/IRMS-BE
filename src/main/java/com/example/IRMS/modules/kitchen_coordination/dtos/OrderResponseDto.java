package com.example.IRMS.modules.kitchen_coordination.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;
import com.example.IRMS.modules.digital_ordering.models.OrderEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderResponseDto {
    @JsonProperty("orderId")
    private Long id;

    @JsonProperty("tableNumber")
    private Long tableId;

    @JsonProperty("items")
    private List<OrderItemResponseDto> items;

    @JsonProperty("totalPrice")
    private Double totalPrice;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime orderTime;

    @JsonProperty("updatedAt")
    private LocalDateTime completedAt;

    public static OrderResponseDto fromEntity(OrderEntity entity) {
        // Convert items to OrderItemResponseDto
        List<OrderItemResponseDto> itemDtos = entity.getItems() != null ?
            entity.getItems().stream()
                .map(OrderItemResponseDto::fromEntity)
                .collect(Collectors.toList()) : List.of();

        // Calculate total price from items
        Double totalPrice = itemDtos.stream()
                .mapToDouble(item -> item.getTotalPrice() != null ? item.getTotalPrice() : 0.0)
                .sum();

        return OrderResponseDto.builder()
                .id(entity.getId())
                .tableId(entity.getTableId())
                .items(itemDtos)
                .totalPrice(totalPrice)
                .status(entity.getStatus())
                .orderTime(entity.getOrderTime())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
