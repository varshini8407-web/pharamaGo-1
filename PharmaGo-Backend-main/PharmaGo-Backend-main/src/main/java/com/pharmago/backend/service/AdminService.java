package com.pharmago.backend.service;

import com.pharmago.backend.dto.AdminStatsResponse;
import com.pharmago.backend.entity.Order;
import com.pharmago.backend.entity.Prescription;
import com.pharmago.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public AdminStatsResponse getStats() {
        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED || o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .totalPharmacies(pharmacyRepository.count())
                .totalMedicines(medicineRepository.count())
                .pendingPrescriptions(prescriptionRepository.findByStatus(Prescription.Status.PENDING).size())
                .totalRevenue(totalRevenue)
                .build();
    }
}
