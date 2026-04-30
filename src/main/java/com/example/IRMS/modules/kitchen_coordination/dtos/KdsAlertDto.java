package com.example.IRMS.modules.kitchen_coordination.dtos;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KdsAlertDto {
    private String type;
    private String message;
    private Long orderId;
    private Long orderItemId;
    private LocalDateTime createdAt;
}
