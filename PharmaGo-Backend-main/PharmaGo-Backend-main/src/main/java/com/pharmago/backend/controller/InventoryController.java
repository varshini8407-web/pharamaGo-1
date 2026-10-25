package com.pharmago.backend.controller;

import com.pharmago.backend.entity.Inventory;
import com.pharmago.backend.entity.Medicine;
import com.pharmago.backend.entity.Pharmacy;
import com.pharmago.backend.service.InventoryService;
import com.pharmago.backend.service.MedicineService;
import com.pharmago.backend.service.PharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PharmacyService pharmacyService;

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<Inventory>> getPharmacyInventory(@PathVariable Long pharmacyId) {
        return pharmacyService.getPharmacyById(pharmacyId)
                .map(pharmacy -> ResponseEntity.ok(inventoryService.getInventoryByPharmacy(pharmacy)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<Inventory>> getMedicineStockAcrossPharmacies(@PathVariable Long medicineId) {
        return medicineService.getMedicineById(medicineId)
                .map(medicine -> ResponseEntity.ok(inventoryService.getPharmaciesByMedicine(medicine)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.updateStock(inventory));
    }
}
