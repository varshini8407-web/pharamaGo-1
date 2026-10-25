package com.example.pharmago.models;

import java.io.Serializable;

/**
 * Result model returned upon successful transactional approval of a prescription order.
 */
public class OrderApprovalResult implements Serializable {
    private long orderId;
    private long invoiceId;
    private String invoiceNumber;
    private String orderType;
    private String qrString;
    private String otpCode;
    private double amount;

    public OrderApprovalResult(long orderId, long invoiceId, String invoiceNumber, String orderType, String qrString, String otpCode, double amount) {
        this.orderId = orderId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.orderType = orderType;
        this.qrString = qrString;
        this.otpCode = otpCode;
        this.amount = amount;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getQrString() {
        return qrString;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public double getAmount() {
        return amount;
    }
}
