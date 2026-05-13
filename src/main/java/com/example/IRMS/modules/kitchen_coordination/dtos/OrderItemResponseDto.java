package com.example.IRMS.modules.kitchen_coordination.dtos;

import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.models.OrderItemEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderItemResponseDto {
    @JsonProperty("orderItemId")
    private Long id;

    @JsonProperty("menuItemId")
    private Long menuItemId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("allergyNotes")
    private String allergyNotes;

    @JsonProperty("specialInstructions")
    private String specialInstructions;

    @JsonProperty("customizations")
    private String customizations;

    @JsonProperty("totalPrice")
    private Double totalPrice;

    @JsonProperty("status")
    private String status;

    @JsonProperty("menuItem")
    private MenuItemEntity menuItem;

    public static OrderItemResponseDto fromEntity(OrderItemEntity entity) {
        Double totalPrice = 0.0;
        if (entity.getMenuItem() != null) {
            totalPrice = entity.getMenuItem().getPrice() * entity.getQuantity();
        }

        // Map OrderItemProgressStatus to frontend status string
        String statusStr = "PENDING";
        if (entity.getProgressStatus() != null) {
            statusStr = entity.getProgressStatus().name();
        }

        return OrderItemResponseDto.builder()
                .id(entity.getId())
                .menuItemId(entity.getMenuItem() != null ? entity.getMenuItem().getId() : null)
                .name(entity.getMenuItem() != null ? entity.getMenuItem().getName() : "")
                .quantity(entity.getQuantity())
                .allergyNotes(entity.getAllergyNotes())
                .specialInstructions(entity.getSpecialInstructions())
                .customizations(entity.getCustomization())
                .totalPrice(totalPrice)
                .status(statusStr)
                .menuItem(entity.getMenuItem())
                .build();
    }
}
