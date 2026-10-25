package com.example.pharmago.models;

/**
 * Member 3: Strict Order Lifecycle Status Enumeration
 */
public enum OrderStatus {
    PENDING_VERIFICATION,
    READY_FOR_PICKUP,
    READY_FOR_DELIVERY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == DELIVERED || this == CANCELLED;
    }

    public boolean isReady() {
        return this == READY_FOR_PICKUP || this == READY_FOR_DELIVERY;
    }
}
