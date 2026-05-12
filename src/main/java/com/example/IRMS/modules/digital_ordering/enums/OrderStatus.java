package com.example.IRMS.modules.digital_ordering.enums;

public enum OrderStatus {
    UNPAID,     // Order has been placed but not yet paid
    PENDING,    // Order has been placed but not yet processed
    IN_PROGRESS, // Order is currently being cooked
    COMPLETED,  // Order has been delivered/picked up
    OVERDUED,   // Order is overdue
    CANCELED   // Order has been canceled
}
