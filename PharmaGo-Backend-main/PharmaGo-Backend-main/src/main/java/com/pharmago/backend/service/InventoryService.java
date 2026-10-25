package com.pharmago.backend.service;

import com.pharmago.backend.entity.Inventory;
import com.pharmago.backend.entity.Medicine;
import com.pharmago.backend.entity.Pharmacy;
import com.pharmago.backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getInventoryByPharmacy(Pharmacy pharmacy) {
        return inventoryRepository.findByPharmacy(pharmacy);
    }

    public List<Inventory> getPharmaciesByMedicine(Medicine medicine) {
        return inventoryRepository.findByMedicine(medicine);
    }

    public Optional<Inventory> getStock(Pharmacy pharmacy, Medicine medicine) {
        return inventoryRepository.findByPharmacyAndMedicine(pharmacy, medicine);
    }

    public Inventory updateStock(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
}
