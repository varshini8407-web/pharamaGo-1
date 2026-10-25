package com.pharmago.backend.service;

import com.pharmago.backend.entity.Prescription;
import com.pharmago.backend.entity.User;
import com.pharmago.backend.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public Prescription savePrescription(User user, String filePath) {
        Prescription prescription = Prescription.builder()
                .user(user)
                .filePath(filePath)
                .status(Prescription.Status.PENDING)
                .build();
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getUserPrescriptions(User user) {
        return prescriptionRepository.findByUser(user);
    }

    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus(Prescription.Status.PENDING);
    }

    public Optional<Prescription> getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id);
    }

    public Prescription verifyPrescription(Long id, Prescription.Status status, String reason) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        
        prescription.setStatus(status);
        prescription.setRejectionReason(reason);
        prescription.setVerifiedAt(LocalDateTime.now());
        
        return prescriptionRepository.save(prescription);
    }
}
