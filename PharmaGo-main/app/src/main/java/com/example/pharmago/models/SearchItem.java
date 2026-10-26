package com.example.pharmago.models;

import java.io.Serializable;

public class SearchItem implements Serializable {
    private Long id;
    private Long pharmacyId;
    private String medicineName;
    private String dosage;
    private String pharmacyName;
    private String price;
    private String category;
    private String description;
    private boolean inStock;
    private boolean requiresPrescription;
    private String itemType; // "MEDICINE", "PHARMACY", "HOSPITAL"

    public SearchItem(Long id, Long pharmacyId, String medicineName, String dosage, String pharmacyName,
                      String price, String category, String description, boolean inStock,
                      boolean requiresPrescription, String itemType) {
        this.id = id;
        this.pharmacyId = pharmacyId != null ? pharmacyId : 1L;
        this.medicineName = medicineName;
        this.dosage = dosage != null ? dosage : "Standard Dosage";
        this.pharmacyName = pharmacyName != null ? pharmacyName : "Apollo Metro Pharmacy";
        this.price = price != null ? price : "₹45.00";
        this.category = category;
        this.description = description;
        this.inStock = inStock;
        this.requiresPrescription = requiresPrescription;
        this.itemType = itemType != null ? itemType : "MEDICINE";
    }

    public SearchItem(Long id, String medicineName, String pharmacyName, String price, String category, String description, boolean requiresPrescription) {
        this(id, 1L, medicineName, "500 mg Tablet", pharmacyName, price, category, description, true, requiresPrescription, "MEDICINE");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPharmacyId() {
        return pharmacyId != null ? pharmacyId : 1L;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage != null ? dosage : "500 mg Tablet";
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public String getItemType() {
        return itemType != null ? itemType : "MEDICINE";
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
