package com.example.pharmago.utils;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.pharmago.R;
import com.example.pharmago.models.OrderModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Member 3: Notification Manager
 * Manages notification channels, system status bar notifications via NotificationCompat.Builder,
 * and local persistent notifications for in-app feeds.
 */
public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static final String PREF_NOTIFICATIONS = "PharmaGoNotifications";

    public static final String CHANNEL_ORDERS = "pharmago_orders_channel";
    public static final String CHANNEL_DELIVERY = "pharmago_delivery_channel";

    /**
     * Initializes Android O+ notification channels
     */
    public static void initChannels(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        android.app.NotificationManager nm = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel orderChannel = new NotificationChannel(
                CHANNEL_ORDERS,
                "Order Status Updates",
                android.app.NotificationManager.IMPORTANCE_HIGH
        );
        orderChannel.setDescription("Notifications for order placement, prescription verification, and pickup readiness");
        orderChannel.enableVibration(true);

        NotificationChannel deliveryChannel = new NotificationChannel(
                CHANNEL_DELIVERY,
                "Live Delivery Tracking",
                android.app.NotificationManager.IMPORTANCE_HIGH
        );
        deliveryChannel.setDescription("Live courier dispatch alerts and delivery verification OTPs");
        deliveryChannel.enableVibration(true);

        nm.createNotificationChannel(orderChannel);
        nm.createNotificationChannel(deliveryChannel);
    }

    /**
     * Requirement 4.3: Order Placed ("Order Placed - Pending Review")
     */
    public static void notifyOrderPlaced(Context context, long orderId) {
        if (context == null) return;
        initChannels(context);

        String title = "Order Placed - Pending Review";
        String message = "Order #" + orderId + " has been placed successfully. Awaiting pharmacist prescription verification.";

        sendSystemNotification(context, (int) orderId, CHANNEL_ORDERS, title, message);
        saveNotificationLocally(context, "NOTIF_PL_" + orderId + "_" + System.currentTimeMillis(), title, message, "ORDER", orderId);
    }

    /**
     * Requirement 4.3: Order Ready for Pickup (Takeaway with QR ready)
     */
    public static void notifyOrderReadyForPickup(Context context, long orderId, String qrPayload) {
        if (context == null) return;
        initChannels(context);

        String title = "Order Ready for Hospital Pickup 🏥";
        String message = "Pharmacist verified Order #" + orderId + ". Your pickup QR pass is ready for counter dispense.";

        sendSystemNotification(context, (int) orderId + 200, CHANNEL_ORDERS, title, message);
        saveNotificationLocally(context, "NOTIF_PK_" + orderId + "_" + System.currentTimeMillis(), title, message, "ORDER", orderId);
    }

    /**
     * Requirement 4.3: Order Out for Delivery (with delivery tracking alert and customer OTP)
     */
    public static void notifyOrderOutForDelivery(Context context, long orderId, String otpCode) {
        if (context == null) return;
        initChannels(context);

        String title = "Order Out for Delivery 🛵";
        String message = "Order #" + orderId + " is on the way! Your delivery verification OTP is [" + (otpCode != null ? otpCode : "549123") + "].";

        sendSystemNotification(context, (int) orderId + 400, CHANNEL_DELIVERY, title, message);
        saveNotificationLocally(context, "NOTIF_OUT_" + orderId + "_" + System.currentTimeMillis(), title, message, "DELIVERY", orderId);
    }

    /**
     * Requirement 4.3: Order Delivered / Completed
     */
    public static void notifyOrderDelivered(Context context, long orderId, String patientName, double totalAmount) {
        notifyDeliveryCompleted(context, orderId, patientName, totalAmount);
    }

    /**
     * Existing notification helper for backward compatibility
     */
    public static void notifyPatientOrderVerified(Context context, OrderModel order) {
        if (context == null || order == null) return;
        initChannels(context);

        if ("TAKEAWAY".equalsIgnoreCase(order.getOrderType())) {
            notifyOrderReadyForPickup(context, order.getOrderId(), order.getQrString());
        } else {
            notifyOrderOutForDelivery(context, order.getOrderId(), order.getOtp());
        }
    }

    /**
     * Existing delivery completed notification helper
     */
    public static void notifyDeliveryCompleted(Context context, long orderId, String patientName, double totalAmount) {
        if (context == null) return;
        initChannels(context);

        String title = "Medication Delivered Successfully 🎉";
        String message = String.format(Locale.getDefault(),
                "Order #%d has been safely delivered to %s. Total: ₹%.2f. Thank you!",
                orderId, patientName != null ? patientName : "Patient", totalAmount);

        sendSystemNotification(context, (int) orderId + 600, CHANNEL_DELIVERY, title, message);
        saveNotificationLocally(context, "NOTIF_DEL_" + orderId + "_" + System.currentTimeMillis(), title, message, "ORDER", orderId);
    }

    private static void sendSystemNotification(Context context, int notificationId, String channelId, String title, String message) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_hospital)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (Exception e) {
            Log.w(TAG, "sendSystemNotification note: " + e.getMessage());
        }
    }

    private static void saveNotificationLocally(Context context, String id, String title, String message, String type, long orderId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NOTIFICATIONS, Context.MODE_PRIVATE);
            JSONArray array = new JSONArray(prefs.getString("notification_items", "[]"));

            String timestamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("message", message);
            obj.put("timestamp", "Just now (" + timestamp + ")");
            obj.put("type", type);
            obj.put("isRead", false);
            obj.put("orderId", orderId);

            array.put(0, obj); // Most recent on top
            prefs.edit().putString("notification_items", array.toString()).apply();
            Log.d(TAG, "Notification recorded locally: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save local notification", e);
        }
    }
}
