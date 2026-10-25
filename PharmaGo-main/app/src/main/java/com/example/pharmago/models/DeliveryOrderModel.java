package com.example.pharmago.models;

import java.io.Serializable;

public class DeliveryOrderModel implements Serializable {
    private Long orderId;
    private String patientName;
    private String patientPhone;
    private String deliveryAddress;
    private String pharmacyAddress;
    private Double totalAmount;
    private String paymentStatus; // "PAID ONLINE", "CASH_ON_DELIVERY"
    private OrderStatus orderStatus; // READY_FOR_DELIVERY, OUT_FOR_DELIVERY, DELIVERED
    private String otpCode;
    private Double latitude;
    private Double longitude;
    private String orderTimestamp;
    private String medicineSummary;

    public DeliveryOrderModel() {}

    public DeliveryOrderModel(Long orderId, String patientName, String patientPhone,
                              String deliveryAddress, String pharmacyAddress, Double totalAmount,
                              String paymentStatus, OrderStatus orderStatus, String otpCode,
                              Double latitude, Double longitude, String orderTimestamp,
                              String medicineSummary) {
        this.orderId = orderId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.deliveryAddress = deliveryAddress;
        this.pharmacyAddress = pharmacyAddress;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.otpCode = otpCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orderTimestamp = orderTimestamp;
        this.medicineSummary = medicineSummary;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress != null ? deliveryAddress : "Patient Address, Metro City";
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPharmacyAddress() {
        return pharmacyAddress != null ? pharmacyAddress : "Apollo Central Pharmacy, Block B";
    }

    public void setPharmacyAddress(String pharmacyAddress) {
        this.pharmacyAddress = pharmacyAddress;
    }

    public Double getTotalAmount() {
        return totalAmount != null ? totalAmount : 0.0;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "PAID ONLINE";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus != null ? orderStatus : OrderStatus.READY_FOR_DELIVERY;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public Double getLatitude() {
        return latitude != null ? latitude : 12.9716;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude != null ? longitude : 77.5946;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getOrderTimestamp() {
        return orderTimestamp != null ? orderTimestamp : "Today";
    }

    public void setOrderTimestamp(String orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public String getMedicineSummary() {
        return medicineSummary != null ? medicineSummary : "Prescribed Medication Package";
    }

    public void setMedicineSummary(String medicineSummary) {
        this.medicineSummary = medicineSummary;
    }

    public boolean isCashOnDelivery() {
        return paymentStatus != null && (paymentStatus.toUpperCase().contains("CASH") || paymentStatus.toUpperCase().contains("COD"));
    }
}
