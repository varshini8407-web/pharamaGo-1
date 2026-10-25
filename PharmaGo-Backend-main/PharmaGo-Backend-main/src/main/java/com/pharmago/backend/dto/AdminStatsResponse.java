package com.pharmago.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalOrders;
    private long totalPharmacies;
    private long totalMedicines;
    private long pendingPrescriptions;
    private double totalRevenue;
}
