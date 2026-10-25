package com.pharmago.backend.service;

import com.pharmago.backend.entity.Pharmacy;
import com.pharmago.backend.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PharmacyService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    public List<Pharmacy> getAllActivePharmacies() {
        return pharmacyRepository.findByActiveTrue();
    }

    public Optional<Pharmacy> getPharmacyById(Long id) {
        return pharmacyRepository.findById(id);
    }

    public Pharmacy savePharmacy(Pharmacy pharmacy) {
        return pharmacyRepository.save(pharmacy);
    }
}
