package com.pharmago.backend.service;

import com.pharmago.backend.entity.*;
import com.pharmago.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Transactional
    public Order createOrder(User user, Pharmacy pharmacy, List<OrderItem> items, Order.OrderType type, Long prescriptionId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> verifiedItems = new ArrayList<>();
        boolean requiresPrescription = false;

        for (OrderItem item : items) {
            // 1. Check if medicine exists in pharmacy inventory
            Inventory inventory = inventoryService.getStock(pharmacy, item.getMedicine())
                    .orElseThrow(() -> new RuntimeException("Medicine " + item.getMedicine().getName() + " not available at this pharmacy"));

            // 2. Check stock quantity
            if (inventory.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + item.getMedicine().getName());
            }

            // 3. Check if medicine requires prescription
            if (item.getMedicine().isRequiresPrescription()) {
                requiresPrescription = true;
            }

            // 4. Calculate item total using server-side price
            item.setPriceAtOrder(inventory.getPrice());
            totalAmount = totalAmount.add(inventory.getPrice().multiply(new BigDecimal(item.getQuantity())));
            
            // 5. Deduct Stock
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryService.updateStock(inventory);

            verifiedItems.add(item);
        }

        Order order = Order.builder()
                .user(user)
                .pharmacy(pharmacy)
                .totalAmount(totalAmount)
                .type(type)
                .status(Order.OrderStatus.PLACED)
                .items(verifiedItems)
                .build();

        // 6. Handle Prescription requirement
        if (requiresPrescription) {
            if (prescriptionId == null) {
                throw new RuntimeException("This order contains medicines that require a prescription.");
            }
            Prescription prescription = prescriptionService.getPrescriptionById(prescriptionId)
                    .orElseThrow(() -> new RuntimeException("Prescription not found"));
            
            if (prescription.getStatus() != Prescription.Status.APPROVED) {
                order.setStatus(Order.OrderStatus.PENDING_VERIFICATION);
            }
            order.setPrescription(prescription);
        }

        // 7. Generate QR Pickup Token if Takeaway
        if (type == Order.OrderType.TAKEAWAY) {
            order.setPickupToken(UUID.randomUUID().toString());
        }

        // Link items to order
        for (OrderItem item : verifiedItems) {
            item.setOrder(order);
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
}
