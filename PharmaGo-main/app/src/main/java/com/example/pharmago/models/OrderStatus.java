package com.example.pharmago.models;

/**
 * Strict Order Lifecycle Status Enumeration
 */
public enum OrderStatus {
    PENDING_APPROVAL,
    PENDING_VERIFICATION,
    APPROVED,
    READY_FOR_PICKUP,
    READY_FOR_DELIVERY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    REJECTED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == DELIVERED || this == CANCELLED || this == REJECTED;
    }

    public boolean isReady() {
        return this == READY_FOR_PICKUP || this == READY_FOR_DELIVERY;
    }

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return PENDING_VERIFICATION;
        }
        String clean = statusStr.trim().toUpperCase().replace(" ", "_");
        for (OrderStatus s : values()) {
            if (s.name().equalsIgnoreCase(clean)) {
                return s;
            }
        }
        if (clean.contains("APPROVAL")) return PENDING_APPROVAL;
        if (clean.contains("APPROV")) return APPROVED;
        if (clean.contains("REJECT")) return REJECTED;
        if (clean.contains("PICKUP")) return READY_FOR_PICKUP;
        if (clean.contains("TRANSIT") || clean.contains("OUT")) return OUT_FOR_DELIVERY;
        if (clean.contains("DELIVERED")) return DELIVERED;
        if (clean.contains("COMPLETE")) return COMPLETED;
        return PENDING_VERIFICATION;
    }
}

