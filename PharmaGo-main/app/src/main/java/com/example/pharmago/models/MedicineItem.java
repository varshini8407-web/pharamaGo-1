package com.example.pharmago.models;

/**
 * MedicineItem model for catalog and cart operations.
 * Extends Medicine for full cross-compatibility.
 */
public class MedicineItem extends Medicine {

    public MedicineItem() {
        super();
    }

    public MedicineItem(Long id, String name, String price, String category, String description, boolean requiresPrescription) {
        super(id, name, price, category, description, requiresPrescription);
    }

    public MedicineItem(Long id, String name, String price, String category, Long pharmacyId, String pharmacyName, boolean requiresPrescription) {
        super(id, name, price, category, pharmacyName, requiresPrescription);
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
    }

    private Long pharmacyId;
    private String pharmacyName;

    public Long getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public MedicineItem(Medicine medicine) {
        if (medicine != null) {
            setId(medicine.getId());
            setName(medicine.getName());
            setPrice(medicine.getPrice());
            setCategory(medicine.getCategory());
            setDescription(medicine.getDescription());
            setRequiresPrescription(medicine.isRequiresPrescription());
        }
    }
}
