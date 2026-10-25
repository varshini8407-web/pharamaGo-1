package com.example.pharmago.models;

import java.io.Serializable;

public class Payment implements Serializable {
    private Long paymentId;
    private Long orderId;
    private double amount;
    private String status; // e.g. "PENDING_COUNTER_PAYMENT", "PAID"
    private String paymentMethod; // e.g. "CASH_ON_PICKUP", "UPI", "CARD"
    private String timestamp;

    public Payment() {}

    public Payment(Long paymentId, Long orderId, double amount, String status, String paymentMethod, String timestamp) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
