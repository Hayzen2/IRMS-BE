package com.example.IRMS.modules.digital_ordering.dtos;
import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    private Long staffId;

    private Long tableId;

    @Valid
    private List<UpdateOrderItemRequest> items;
}
