package com.example.IRMS.modules.digital_ordering.enums;

public enum OrderStatus {
    PENDING,    // Order has been placed but not yet processed
    COOKING,    // Order is currently being cooked
    READY,      // Order is ready to be served or picked up
    COMPLETED,  // Order has been delivered/picked up
    CANCELED   // Order has been canceled
}
