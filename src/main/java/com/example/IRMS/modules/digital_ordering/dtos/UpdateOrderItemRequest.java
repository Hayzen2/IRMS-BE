package com.example.IRMS.modules.digital_ordering.dtos;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateOrderItemRequest {
    private Long itemId;
    private Long menuItemId;

    @Min(1)
    private int quantity;

    private String specialInstructions;
    private String allergyNotes;
    private String customization;

    private boolean canceled;
}
