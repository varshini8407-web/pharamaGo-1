package com.pharmago.backend.repository;

import com.pharmago.backend.entity.Order;
import com.pharmago.backend.entity.Pharmacy;
import com.pharmago.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByPharmacy(Pharmacy pharmacy);
    Optional<Order> findByPickupToken(String pickupToken);
}
