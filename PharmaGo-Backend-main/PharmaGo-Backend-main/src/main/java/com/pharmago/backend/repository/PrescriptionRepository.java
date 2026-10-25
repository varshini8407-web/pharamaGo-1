package com.pharmago.backend.repository;

import com.pharmago.backend.entity.Prescription;
import com.pharmago.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByUser(User user);
    List<Prescription> findByStatus(Prescription.Status status);
}
