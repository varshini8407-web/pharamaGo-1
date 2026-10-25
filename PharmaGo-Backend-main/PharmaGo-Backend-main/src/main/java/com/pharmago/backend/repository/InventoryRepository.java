package com.pharmago.backend.repository;

import com.pharmago.backend.entity.Inventory;
import com.pharmago.backend.entity.Medicine;
import com.pharmago.backend.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByPharmacy(Pharmacy pharmacy);
    List<Inventory> findByMedicine(Medicine medicine);
    Optional<Inventory> findByPharmacyAndMedicine(Pharmacy pharmacy, Medicine medicine);
}
