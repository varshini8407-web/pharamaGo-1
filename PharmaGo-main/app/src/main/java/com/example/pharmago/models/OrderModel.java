package com.example.pharmago.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderModel implements Serializable {
    private Long orderId;
    private String patientName;
    private String patientPhone;
    private String orderType; // "TAKEAWAY" or "DELIVERY"
    private OrderStatus status; // PENDING_VERIFICATION, READY_FOR_PICKUP, READY_FOR_DELIVERY, etc.
    private String prescriptionImageUrl;
    private Double totalAmount;
    private String otp;
    private byte[] qrCodeBytes;
    private String qrCodeString;
    private String orderTimestamp;
    private String deliveryAddress;
    private Long invoiceId;
    private String paymentStatus = "PAID ONLINE (UPI)";
    private String rejectionReason;
    private boolean prescriptionRequired = true;
    private List<PrescriptionMedicineItem> medicineList = new ArrayList<>();

    public OrderModel() {}

    public OrderModel(Long orderId, String patientName, String patientPhone, String orderType,
                      OrderStatus status, String prescriptionImageUrl, Double totalAmount,
                      String orderTimestamp, String deliveryAddress,
                      List<PrescriptionMedicineItem> medicineList) {
        this.orderId = orderId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.orderType = orderType;
        this.status = status;
        this.prescriptionImageUrl = prescriptionImageUrl;
        this.totalAmount = totalAmount;
        this.orderTimestamp = orderTimestamp;
        this.deliveryAddress = deliveryAddress;
        this.medicineList = medicineList != null ? medicineList : new ArrayList<>();
    }

    public OrderModel(Long orderId, String patientName, String patientPhone, String orderType,
                      OrderStatus status, String prescriptionImageUrl, Double totalAmount,
                      String otp, String qrCodeString, byte[] qrCodeBytes, String orderTimestamp,
                      String deliveryAddress, Long invoiceId, String paymentStatus,
                      boolean prescriptionRequired, int extra) {
        this.orderId = orderId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.orderType = orderType;
        this.status = status;
        this.prescriptionImageUrl = prescriptionImageUrl;
        this.totalAmount = totalAmount;
        this.otp = otp;
        this.qrCodeString = qrCodeString;
        this.qrCodeBytes = qrCodeBytes;
        this.orderTimestamp = orderTimestamp;
        this.deliveryAddress = deliveryAddress;
        this.invoiceId = invoiceId;
        this.paymentStatus = paymentStatus;
        this.prescriptionRequired = prescriptionRequired;
    }

    public OrderStatus getOrderStatus() {
        return getStatus();
    }

    public String getTimestamp() {
        return getOrderTimestamp();
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

    public String getOrderType() {
        return orderType != null ? orderType.toUpperCase() : "DELIVERY";
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getStatus() {
        return status != null ? status : OrderStatus.PENDING_VERIFICATION;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPrescriptionImageUrl() {
        return prescriptionImageUrl;
    }

    public void setPrescriptionImageUrl(String prescriptionImageUrl) {
        this.prescriptionImageUrl = prescriptionImageUrl;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public byte[] getQrCodeBytes() {
        return qrCodeBytes;
    }

    public void setQrCodeBytes(byte[] qrCodeBytes) {
        this.qrCodeBytes = qrCodeBytes;
    }

    public String getQrCodeString() {
        return qrCodeString;
    }

    public String getQrString() {
        return qrCodeString;
    }

    public void setQrCodeString(String qrCodeString) {
        this.qrCodeString = qrCodeString;
    }

    public void setQrString(String qrString) {
        this.qrCodeString = qrString;
    }

    public String getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(String orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public List<PrescriptionMedicineItem> getMedicineList() {
        return medicineList;
    }

    public void setMedicineList(List<PrescriptionMedicineItem> medicineList) {
        this.medicineList = medicineList;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "PAID ONLINE (UPI)";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public double calculateSubtotal() {
        double sum = 0.0;
        if (medicineList != null) {
            for (PrescriptionMedicineItem item : medicineList) {
                sum += item.getSubtotal();
            }
        }
        return sum;
    }
}
