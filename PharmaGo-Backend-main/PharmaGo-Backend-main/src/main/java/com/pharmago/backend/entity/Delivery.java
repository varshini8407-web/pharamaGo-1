package com.pharmago.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @NotNull
    private Order order;

    private String deliveryPersonName;
    private String deliveryPersonPhone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    private String deliveryAddress;

    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    public enum DeliveryStatus {
        PENDING, ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = DeliveryStatus.PENDING;
        }
    }
}
