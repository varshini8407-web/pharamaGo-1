package com.pharmago.backend.controller;

import com.pharmago.backend.entity.Order;
import com.pharmago.backend.repository.OrderRepository;
import com.pharmago.backend.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pickup")
public class PickupController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping("/qr/{orderId}")
    public ResponseEntity<?> getOrderQRCode(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getType() != Order.OrderType.TAKEAWAY) {
            return ResponseEntity.badRequest().body("QR Code is only available for Takeaway orders.");
        }

        if (order.getPickupToken() == null) {
            return ResponseEntity.badRequest().body("Pickup token not generated for this order.");
        }

        String qrBase64 = qrCodeService.generateQRCodeBase64(order.getPickupToken(), 300, 300);
        return ResponseEntity.ok(Map.of("qrCode", qrBase64));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validatePickup(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Order order = orderRepository.findByPickupToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Pickup Token"));

        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Order already collected.");
        }

        if (order.getStatus() != Order.OrderStatus.READY_FOR_PICKUP && order.getStatus() != Order.OrderStatus.CONFIRMED) {
             return ResponseEntity.badRequest().body("Order is not ready for pickup. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);

        return ResponseEntity.ok("Pickup successful! Order #" + order.getId() + " marked as completed.");
    }
}
