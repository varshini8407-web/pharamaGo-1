package com.example.pharmago.models;

import java.io.Serializable;

public class InventoryItem implements Serializable {
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private int availableStock;
    private double unitPrice;
    private String category;
    private String batchNo;
    private String expiryDate;
    private int dispensedCount;
    private boolean rxRequired;

    public InventoryItem() {}

    public InventoryItem(Long medicineId, String medicineName, String dosage, int availableStock,
                         double unitPrice, String category, String batchNo, String expiryDate, int dispensedCount) {
        this(medicineId, medicineName, dosage, availableStock, unitPrice, category, batchNo, expiryDate, dispensedCount, false);
    }

    public InventoryItem(Long medicineId, String medicineName, String dosage, int availableStock,
                         double unitPrice, String category, String batchNo, String expiryDate, int dispensedCount, boolean rxRequired) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.availableStock = availableStock;
        this.unitPrice = unitPrice;
        this.category = category;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.dispensedCount = dispensedCount;
        this.rxRequired = rxRequired;
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

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBatchNo() {
        return batchNo != null ? batchNo : "BATCH-GEN-2026";
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getExpiryDate() {
        return expiryDate != null ? expiryDate : "12/2027";
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getDispensedCount() {
        return dispensedCount;
    }

    public void setDispensedCount(int dispensedCount) {
        this.dispensedCount = dispensedCount;
    }

    public Long getId() {
        return medicineId;
    }

    public String getBrandName() {
        return medicineName;
    }

    public int getStockQty() {
        return availableStock;
    }

    public boolean isRxRequired() {
        return rxRequired;
    }

    public void setRxRequired(boolean rxRequired) {
        this.rxRequired = rxRequired;
    }
}
