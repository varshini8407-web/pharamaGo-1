package com.pharmago.backend.dto;

import com.pharmago.backend.entity.Order;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull
    private Long pharmacyId;

    @NotEmpty
    private List<OrderItemRequest> items;

    @NotNull
    private Order.OrderType type;

    private Long prescriptionId;
    
    private String deliveryAddress;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long medicineId;
        @NotNull
        private Integer quantity;
    }
}
