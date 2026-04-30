package com.example.IRMS.modules.digital_ordering.enums;

public enum TableStatus {
    AVAILABLE,   // Table is free and can be occupied
    OCCUPIED,    // Table is currently occupied by customers
    RESERVED,    // Table is reserved for a future time
    NEEDS_CLEANING // Table has been vacated and needs cleaning before it can be used again   
}
