package com.example.IRMS.modules.digital_ordering.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long tableId;

    @Valid
    @NotEmpty
    private List<CreateOrderItemRequest> items;
}
