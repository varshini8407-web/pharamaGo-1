package com.pharmago.backend.controller;

import com.pharmago.backend.entity.Delivery;
import com.pharmago.backend.entity.Order;
import com.pharmago.backend.repository.OrderRepository;
import com.pharmago.backend.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDeliveryByOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return deliveryService.getDeliveryByOrder(order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{deliveryId}/assign")
    public ResponseEntity<Delivery> assignDelivery(@PathVariable Long deliveryId, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String phone = request.get("phone");
        return ResponseEntity.ok(deliveryService.assignDelivery(deliveryId, name, phone));
    }

    @PostMapping("/{deliveryId}/status")
    public ResponseEntity<Delivery> updateDeliveryStatus(@PathVariable Long deliveryId, @RequestBody Map<String, String> request) {
        Delivery.DeliveryStatus status = Delivery.DeliveryStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(deliveryService.updateStatus(deliveryId, status));
    }
}
