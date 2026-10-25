package com.example.pharmago.models;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String time;
    private String type; // ORDER, PRESCRIPTION, REMINDER, PROMO
    private boolean isRead;
    private Long orderId;

    public NotificationModel(String id, String title, String message, String time, String type, boolean isRead, Long orderId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.isRead = isRead;
        this.orderId = orderId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Long getOrderId() { return orderId; }
}
