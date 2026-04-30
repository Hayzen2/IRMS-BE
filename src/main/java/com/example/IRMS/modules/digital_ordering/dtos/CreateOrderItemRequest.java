package com.example.IRMS.modules.digital_ordering.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItemRequest {
    @NotNull
    private Long menuItemId;

    @Min(1)
    private int quantity;

    private String specialInstructions;
    private String allergyNotes;
    private String customization;
}
