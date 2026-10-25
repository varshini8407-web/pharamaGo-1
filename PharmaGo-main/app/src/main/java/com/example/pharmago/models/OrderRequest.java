package com.example.pharmago.models;

import java.util.List;

public class OrderRequest {
    private Long pharmacyId;
    private List<OrderItemRequest> items;
    private String type; // TAKEAWAY, DELIVERY
    private Long prescriptionId;
    private String deliveryAddress;

    public OrderRequest(Long pharmacyId, List<OrderItemRequest> items, String type, Long prescriptionId, String deliveryAddress) {
        this.pharmacyId = pharmacyId;
        this.items = items;
        this.type = type;
        this.prescriptionId = prescriptionId;
        this.deliveryAddress = deliveryAddress;
    }

    public static class OrderItemRequest {
        private Long medicineId;
        private Integer quantity;

        public OrderItemRequest(Long medicineId, Integer quantity) {
            this.medicineId = medicineId;
            this.quantity = quantity;
        }
    }
}