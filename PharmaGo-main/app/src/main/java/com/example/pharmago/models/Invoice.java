package com.example.pharmago.models;

import android.content.Context;
import android.util.Log;

import com.example.pharmago.database.DatabaseConnection;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Member 2 & 3: Clinical Invoice Model & Generator
 */
public class Invoice implements Serializable {
    private static final String TAG = "Invoice";

    private Long invoiceId;
    private Long orderId;
    private String pharmacistId; // e.g. "ITA1024"
    private double subtotal;
    private double tax;
    private double platformFee;
    private double grandTotal;
    private String timestamp;

    public Invoice() {}

    public Invoice(Long invoiceId, Long orderId, String pharmacistId, double subtotal, double tax, double platformFee, double grandTotal, String timestamp) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.pharmacistId = pharmacistId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.platformFee = platformFee;
        this.grandTotal = grandTotal;
        this.timestamp = timestamp;
    }

    /**
     * Requirement 4.2: Computes total amount and inserts a record into invoices table in MySQL/SQLite
     *
     * @param orderId The target order identifier
     * @param subtotal Net amount before taxes
     * @param tax Applicable medicine GST / clinical taxes
     * @param paymentMode Payment method (e.g., "UPI", "CASH", "PENDING")
     * @return Generated Invoice object
     */
    public static Invoice generateInvoice(int orderId, double subtotal, double tax, String paymentMode) {
        return generateInvoice((long) orderId, subtotal, tax, paymentMode, null);
    }

    public static Invoice generateInvoice(long orderId, double subtotal, double tax, String paymentMode, Context context) {
        double platformFee = 5.0; // standard clinical platform fee
        double grandTotal = Math.round((subtotal + tax + platformFee) * 100.0) / 100.0;
        String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Invoice inv = new Invoice();
        long generatedInvId = 1000L + orderId;
        inv.setInvoiceId(generatedInvId);
        inv.setOrderId(orderId);
        inv.setPharmacistId("ITA1024");
        inv.setSubtotal(subtotal);
        inv.setTax(tax);
        inv.setPlatformFee(platformFee);
        inv.setGrandTotal(grandTotal);
        inv.setTimestamp(nowStr);

        // Async persistence to MySQL & SQLite off main thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Local SQLite persistence
                DatabaseConnection db = (context != null) ? DatabaseConnection.getInstance(context) : DatabaseConnection.getInstance();
                if (db != null) {
                    db.saveInvoice(inv);
                }

                // Direct MySQL JDBC persistence
                if (db != null) {
                    try (Connection conn = db.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "INSERT INTO invoices (id, order_id, pharmacist_id, subtotal, tax, platform_fee, grand_total, payment_status, created_at) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                                         "ON DUPLICATE KEY UPDATE grand_total = VALUES(grand_total), payment_status = VALUES(payment_status)"
                         )) {
                        stmt.setLong(1, generatedInvId);
                        stmt.setLong(2, orderId);
                        stmt.setString(3, "ITA1024");
                        stmt.setDouble(4, subtotal);
                        stmt.setDouble(5, tax);
                        stmt.setDouble(6, platformFee);
                        stmt.setDouble(7, grandTotal);
                        stmt.setString(8, paymentMode != null ? paymentMode : "PENDING");
                        stmt.executeUpdate();
                        Log.i(TAG, "Generated invoice record #" + generatedInvId + " in MySQL for Order #" + orderId);
                    } catch (Exception mySqlEx) {
                        Log.w(TAG, "Direct MySQL invoice insert note: " + mySqlEx.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to persist invoice for order #" + orderId, e);
            }
        });

        return inv;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPharmacistId() {
        return pharmacistId;
    }

    public void setPharmacistId(String pharmacistId) {
        this.pharmacistId = pharmacistId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(double platformFee) {
        this.platformFee = platformFee;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
