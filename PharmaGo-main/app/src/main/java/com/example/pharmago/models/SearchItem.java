package com.example.pharmago.models;

public class SearchItem {
    private Long id;
    private String medicineName;
    private String pharmacyName;
    private String price;
    private String category;
    private String description;
    private boolean requiresPrescription;

    public SearchItem(Long id, String medicineName, String pharmacyName, String price, String category, String description, boolean requiresPrescription) {
        this.id = id;
        this.medicineName = medicineName;
        this.pharmacyName = pharmacyName;
        this.price = price;
        this.category = category;
        this.description = description;
        this.requiresPrescription = requiresPrescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
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

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }
}
