package com.example.pharmago.models;

import java.io.Serializable;

/**
 * Authentication Result model containing user profile and role details.
 * Roles: "PATIENT" / "CUSTOMER", "PHARMACIST", "DELIVERY", "ADMIN".
 */
public class UserAuthResult implements Serializable {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String token;

    public UserAuthResult(Long userId, String username, String fullName, String email, String phone, String role, String token) {
        this(userId, username, fullName, email, phone, "Apollo Sector 4, Metro City", role, token);
    }

    public UserAuthResult(Long userId, String username, String fullName, String email, String phone, String address, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address != null ? address : "Apollo Sector 4, Metro City";
        this.role = role != null ? role.toUpperCase() : "CUSTOMER";
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public boolean isPharmacist() {
        return "PHARMACIST".equalsIgnoreCase(role) || (username != null && username.toUpperCase().startsWith("ITA"));
    }

    public boolean isDelivery() {
        return "DELIVERY".equalsIgnoreCase(role) || (username != null && (username.toUpperCase().startsWith("AGENT") || username.toUpperCase().startsWith("DEL")));
    }

    public boolean isCustomer() {
        return !isPharmacist() && !isDelivery();
    }
}
