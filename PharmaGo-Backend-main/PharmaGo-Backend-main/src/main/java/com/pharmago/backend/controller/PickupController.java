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
            order.setPickupToken("ORDER:" + order.getId() + ":INV:" + (order.getId() + 1000));
            orderRepository.save(order);
        }

        String qrBase64 = qrCodeService.generateQRCodeBase64(order.getPickupToken(), 300, 300);
        return ResponseEntity.ok(Map.of("qrCode", qrBase64));
    }

    @PostMapping("/lookup")
    public ResponseEntity<?> lookupPickup(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Order order = findOrderByTokenOrId(token);
        if (order == null) {
            return ResponseEntity.badRequest().body("No order found matching token: " + token);
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validatePickup(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Order order = findOrderByTokenOrId(token);
        if (order == null) {
            return ResponseEntity.badRequest().body("Invalid Pickup Token");
        }

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

    private Order findOrderByTokenOrId(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        token = token.trim();

        // 1. Exact match on pickupToken
        Order order = orderRepository.findByPickupToken(token).orElse(null);
        if (order != null) return order;

        // 2. Extract ID from ORDER:<id>:INV:<invId>
        if (token.toUpperCase().contains("ORDER:")) {
            try {
                String[] parts = token.split(":");
                for (int i = 0; i < parts.length; i++) {
                    if ("ORDER".equalsIgnoreCase(parts[i]) && i + 1 < parts.length) {
                        long oId = Long.parseLong(parts[i + 1]);
                        order = orderRepository.findById(oId).orElse(null);
                        if (order != null) return order;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 3. Raw numeric ID
        try {
            long oId = Long.parseLong(token);
            return orderRepository.findById(oId).orElse(null);
        } catch (Exception ignored) {}

        return null;
    }
}
