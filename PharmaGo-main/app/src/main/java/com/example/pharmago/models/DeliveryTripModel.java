package com.example.pharmago.models;

import java.io.Serializable;

public class DeliveryTripModel implements Serializable {
    private long id;
    private long orderId;
    private long agentId;
    private double distanceKm;
    private double payout;
    private double tip;
    private String completedAt;

    public DeliveryTripModel(long id, long orderId, long agentId, double distanceKm, double payout, double tip, String completedAt) {
        this.id = id;
        this.orderId = orderId;
        this.agentId = agentId;
        this.distanceKm = distanceKm;
        this.payout = payout;
        this.tip = tip;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getAgentId() {
        return agentId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getPayout() {
        return payout;
    }

    public double getTip() {
        return tip;
    }

    public double getTotalEarning() {
        return payout + tip;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}
