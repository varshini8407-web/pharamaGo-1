package com.example.pharmago.models;

public class Medicine {
    private Long id;
    private String name;
    private String description;
    private String manufacturer;
    private String category;
    private String price; // Added field based on usage in MedicineSearchActivity
    private boolean requiresPrescription;
    private String createdAt;

    public Medicine() {}

    public Medicine(Long id, String name, String price, String category, String description, boolean requiresPrescription) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.requiresPrescription = requiresPrescription;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrice() {
        if (price == null || price.trim().isEmpty()) return "₹45.00";
        if (!price.contains("₹")) return "₹" + price;
        return price;
    }
    public void setPrice(String price) { this.price = price; }

    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}