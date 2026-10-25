package com.example.pharmago.models;

import java.util.List;

public class OrderResponse {
    private Long id;
    private String status;
    private String orderDate;
    private String type; // TAKEAWAY, DELIVERY
    private String deliveryAddress;
    private Double totalPrice;
    private String pharmacyName;
    private List<OrderItem> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public static class OrderItem {
        private String medicineName;
        private Integer quantity;
        private Double price;

        public String getMedicineName() { return medicineName; }
        public Integer getQuantity() { return quantity; }
        public Double getPrice() { return price; }
    }
}