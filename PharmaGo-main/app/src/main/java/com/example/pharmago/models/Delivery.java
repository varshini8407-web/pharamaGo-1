package com.example.pharmago.models;

import android.location.Location;
import java.io.Serializable;

/**
 * Member 3 Smart Features: Delivery Routing Entity
 * Holds delivery metadata, geolocations, estimated ETA, and calculates distances.
 */
public class Delivery extends DeliveryOrderModel implements Serializable {

    private Long deliveryId;
    private Long orderId;
    private String patientAddress;
    private String pharmacyAddress;
    private Double patientLat;
    private Double patientLng;
    private Double pharmacyLat;
    private Double pharmacyLng;
    private String estimatedTime;
    private Double distanceKm;

    public Delivery() {
        super();
        this.pharmacyLat = 12.9716;
        this.pharmacyLng = 77.5946;
        this.patientLat = 12.9815;
        this.patientLng = 77.6050;
        this.estimatedTime = "~10 mins";
        this.distanceKm = calculateDistanceKm();
    }

    public Delivery(Long deliveryId, Long orderId, String patientAddress, String pharmacyAddress,
                    Double patientLat, Double patientLng, Double pharmacyLat, Double pharmacyLng,
                    String estimatedTime, Double distanceKm) {
        super();
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.patientAddress = patientAddress;
        this.pharmacyAddress = pharmacyAddress;
        this.patientLat = patientLat != null ? patientLat : 12.9815;
        this.patientLng = patientLng != null ? patientLng : 77.6050;
        this.pharmacyLat = pharmacyLat != null ? pharmacyLat : 12.9716;
        this.pharmacyLng = pharmacyLng != null ? pharmacyLng : 77.5946;
        this.estimatedTime = estimatedTime != null ? estimatedTime : "~10 mins";
        this.distanceKm = distanceKm != null ? distanceKm : calculateDistanceKm();

        // Also synchronize super fields
        setOrderId(orderId);
        setDeliveryAddress(patientAddress);
        setPharmacyAddress(pharmacyAddress);
        setLatitude(this.patientLat);
        setLongitude(this.patientLng);
    }

    public Delivery(Long orderId, String patientName, String patientPhone,
                    String deliveryAddress, String pharmacyAddress, Double totalAmount,
                    String paymentStatus, OrderStatus orderStatus, String otpCode,
                    Double latitude, Double longitude, String orderTimestamp,
                    String medicineSummary) {
        super(orderId, patientName, patientPhone, deliveryAddress, pharmacyAddress,
                totalAmount, paymentStatus, orderStatus, otpCode, latitude, longitude,
                orderTimestamp, medicineSummary);
        this.orderId = orderId;
        this.patientAddress = deliveryAddress;
        this.pharmacyAddress = pharmacyAddress;
        this.patientLat = latitude != null ? latitude : 12.9815;
        this.patientLng = longitude != null ? longitude : 77.6050;
        this.pharmacyLat = 12.9716;
        this.pharmacyLng = 77.5946;
        this.estimatedTime = "~10 mins";
        this.distanceKm = calculateDistanceKm();
    }

    /**
     * Helper method to calculate straight-line road distance using android.location.Location.distanceBetween()
     */
    public static double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        double km = results[0] / 1000.0;
        return Math.round(km * 10.0) / 10.0;
    }

    public double calculateDistanceKm() {
        if (patientLat != null && patientLng != null && pharmacyLat != null && pharmacyLng != null) {
            this.distanceKm = calculateDistanceKm(pharmacyLat, pharmacyLng, patientLat, patientLng);
            return this.distanceKm;
        }
        return this.distanceKm != null ? this.distanceKm : 2.4;
    }

    // Getters and Setters
    public Long getDeliveryId() {
        return deliveryId != null ? deliveryId : (orderId != null ? orderId + 5000L : 5001L);
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    @Override
    public Long getOrderId() {
        return orderId != null ? orderId : super.getOrderId();
    }

    @Override
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
        super.setOrderId(orderId);
    }

    @Override
    public String getDeliveryAddress() {
        return patientAddress != null ? patientAddress : super.getDeliveryAddress();
    }

    public String getPatientAddress() {
        return getDeliveryAddress();
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
        super.setDeliveryAddress(patientAddress);
    }

    @Override
    public String getPharmacyAddress() {
        return pharmacyAddress != null ? pharmacyAddress : super.getPharmacyAddress();
    }

    @Override
    public void setPharmacyAddress(String pharmacyAddress) {
        this.pharmacyAddress = pharmacyAddress;
        super.setPharmacyAddress(pharmacyAddress);
    }

    public Double getPatientLat() {
        return patientLat != null ? patientLat : getLatitude();
    }

    public void setPatientLat(Double patientLat) {
        this.patientLat = patientLat;
        setLatitude(patientLat);
    }

    public Double getPatientLng() {
        return patientLng != null ? patientLng : getLongitude();
    }

    public void setPatientLng(Double patientLng) {
        this.patientLng = patientLng;
        setLongitude(patientLng);
    }

    public Double getPharmacyLat() {
        return pharmacyLat != null ? pharmacyLat : 12.9716;
    }

    public void setPharmacyLat(Double pharmacyLat) {
        this.pharmacyLat = pharmacyLat;
    }

    public Double getPharmacyLng() {
        return pharmacyLng != null ? pharmacyLng : 77.5946;
    }

    public void setPharmacyLng(Double pharmacyLng) {
        this.pharmacyLng = pharmacyLng;
    }

    public String getEstimatedTime() {
        return estimatedTime != null ? estimatedTime : "~10 mins";
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public Double getDistanceKm() {
        return distanceKm != null ? distanceKm : calculateDistanceKm();
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
