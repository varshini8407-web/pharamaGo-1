package com.example.pharmago.models;

import java.io.Serializable;

public class PrescriptionMedicineItem implements Serializable {
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private int requestedQty;
    private int availableStock;
    private double unitPrice;

    public PrescriptionMedicineItem() {}

    public PrescriptionMedicineItem(Long medicineId, String medicineName, String dosage, int requestedQty, int availableStock, double unitPrice) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.requestedQty = requestedQty;
        this.availableStock = availableStock;
        this.unitPrice = unitPrice;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getRequestedQty() {
        return requestedQty;
    }

    public void setRequestedQty(int requestedQty) {
        this.requestedQty = requestedQty;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return requestedQty * unitPrice;
    }

    public boolean isInStock() {
        return availableStock > 0 && availableStock >= requestedQty;
    }
}
