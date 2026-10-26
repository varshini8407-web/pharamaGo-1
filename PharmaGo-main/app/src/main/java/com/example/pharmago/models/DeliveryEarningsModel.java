package com.example.pharmago.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeliveryEarningsModel implements Serializable {
    private long totalTripsToday;
    private double totalEarningsToday;
    private double weeklyEarningsTotal;
    private List<DeliveryTripModel> tripHistory;
    private double[] weeklyBreakdown; // Mon-Sun

    public DeliveryEarningsModel(long totalTripsToday, double totalEarningsToday, double weeklyEarningsTotal, List<DeliveryTripModel> tripHistory, double[] weeklyBreakdown) {
        this.totalTripsToday = totalTripsToday;
        this.totalEarningsToday = totalEarningsToday;
        this.weeklyEarningsTotal = weeklyEarningsTotal;
        this.tripHistory = tripHistory != null ? tripHistory : new ArrayList<>();
        this.weeklyBreakdown = weeklyBreakdown != null ? weeklyBreakdown : new double[]{240.0, 310.0, 180.0, 420.0, 350.0, 490.0, totalEarningsToday};
    }

    public long getTotalTripsToday() {
        return totalTripsToday;
    }

    public double getTotalEarningsToday() {
        return totalEarningsToday;
    }

    public double getWeeklyEarningsTotal() {
        return weeklyEarningsTotal;
    }

    public List<DeliveryTripModel> getTripHistory() {
        return tripHistory;
    }

    public double[] getWeeklyBreakdown() {
        return weeklyBreakdown;
    }
}
