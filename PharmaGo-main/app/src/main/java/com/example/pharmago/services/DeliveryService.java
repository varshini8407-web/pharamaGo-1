package com.example.pharmago.services;

import android.content.Context;
import android.util.Log;

import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.utils.NotificationManager;

import java.util.List;
import java.util.Map;

/**
 * Member 3: Delivery Service Bridge
 * Executes JDBC / SQLite queries to assign orders, update OrderStatus to "OUT_FOR_DELIVERY" / "DELIVERED",
 * and verify OTP values.
 */
public class DeliveryService {
    private static final String TAG = "DeliveryService";
    private static DeliveryService instance;

    private final DatabaseConnection databaseConnection;

    public static synchronized DeliveryService getInstance(Context context) {
        if (instance == null) {
            instance = new DeliveryService(context.getApplicationContext());
        }
        return instance;
    }

    private DeliveryService(Context context) {
        this.databaseConnection = DatabaseConnection.getInstance(context);
    }

    /**
     * Requirement 2: Orders Feed & Tab Categorization
     * @param filter "NEW" (READY_FOR_DELIVERY), "ACTIVE" (OUT_FOR_DELIVERY), "HISTORY" (DELIVERED)
     */
    public List<DeliveryOrderModel> getDeliveryOrders(String filter) {
        return databaseConnection.getDeliveryOrders(filter);
    }

    /**
     * Requirement 1: Agent Header summary cards
     */
    public Map<String, Integer> getDeliveryMetrics() {
        return databaseConnection.getDeliverySummaryMetrics();
    }

    /**
     * Requirement 2: Accept Order / Start Delivery (transitions to OUT_FOR_DELIVERY)
     */
    public boolean acceptAndStartDelivery(long orderId) {
        Log.d(TAG, "Starting delivery for Order #" + orderId + " -> OUT_FOR_DELIVERY");
        return databaseConnection.updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY);
    }

    /**
     * Requirement 4: Secure 6-Digit OTP Delivery Verification
     * Validates entered OTP against DB value, transitions OrderStatus to DELIVERED,
     * updates CASH_ON_DELIVERY payment to PAID, and notifies patient & pharmacist.
     */
    public boolean verifyOtpAndCompleteDelivery(Context context, DeliveryOrderModel order, String enteredOtp) {
        if (order == null || enteredOtp == null) return false;
        long orderId = order.getOrderId();

        boolean verified = databaseConnection.verifyDeliveryOtp(orderId, enteredOtp);
        if (verified) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            if (order.isCashOnDelivery()) {
                order.setPaymentStatus("PAID (Cash Collected)");
            }

            // Requirement 4.b: Alert patient and pharmacist of successful delivery
            NotificationManager.notifyDeliveryCompleted(
                    context,
                    orderId,
                    order.getPatientName(),
                    order.getTotalAmount()
            );
            Log.d(TAG, "Order #" + orderId + " successfully verified with OTP and marked DELIVERED.");
        } else {
            Log.w(TAG, "OTP verification failed for Order #" + orderId + ". Provided: " + enteredOtp);
        }
        return verified;
    }
}
