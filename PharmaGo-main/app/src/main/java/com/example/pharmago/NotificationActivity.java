package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.adapters.NotificationAdapter;
import com.example.pharmago.models.NotificationModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private MaterialCardView layoutEmptyNotifications;
    private View btnBackNotifications;
    private TextView btnClearNotifications, tvNotificationSubtitle;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rvNotifications);
        layoutEmptyNotifications = findViewById(R.id.layoutEmptyNotifications);
        btnBackNotifications = findViewById(R.id.btnBackNotifications);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        tvNotificationSubtitle = findViewById(R.id.tvNotificationSubtitle);

        if (btnBackNotifications != null) {
            btnBackNotifications.setOnClickListener(v -> finish());
        }

        notificationList = buildMockNotifications();

        adapter = new NotificationAdapter(notificationList, notification -> {
            if ("ORDER".equalsIgnoreCase(notification.getType())) {
                Intent intent = new Intent(NotificationActivity.this, DeliveryTrackingActivity.class);
                intent.putExtra("orderId", notification.getOrderId() != null ? notification.getOrderId() : 103L);
                startActivity(intent);
            } else if ("PRESCRIPTION".equalsIgnoreCase(notification.getType())) {
                Intent intent = new Intent(NotificationActivity.this, PrescriptionUploadActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(NotificationActivity.this, notification.getTitle() + " opened", Toast.LENGTH_SHORT).show();
            }
        });

        if (rvNotifications != null) {
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }

        if (btnClearNotifications != null) {
            btnClearNotifications.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.clearAll();
                }
                if (layoutEmptyNotifications != null) {
                    layoutEmptyNotifications.setVisibility(View.VISIBLE);
                }
                if (tvNotificationSubtitle != null) {
                    tvNotificationSubtitle.setText("0 unread notifications");
                }
                Toast.makeText(NotificationActivity.this, "All notifications cleared", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private List<NotificationModel> buildMockNotifications() {
        List<NotificationModel> list = new ArrayList<>();

        list.add(new NotificationModel(
                "N1",
                "Order Out for Delivery 🚴",
                "Your medicines from CarePoint Chemist (Order #103) are out for delivery with Rajesh K. (Est. 15 mins).",
                "5 mins ago",
                "ORDER",
                false,
                103L
        ));

        list.add(new NotificationModel(
                "N2",
                "Prescription Verified & Approved 🩺",
                "Dr. Arvind Sharma (Apollo Clinic) verified your digital prescription #RX-8942 for Amoxicillin & Paracetamol.",
                "35 mins ago",
                "PRESCRIPTION",
                false,
                null
        ));

        list.add(new NotificationModel(
                "N3",
                "Medication Dose Reminder 💊",
                "Time for your afternoon dose: Paracetamol 650mg (1 tablet after lunch). Tap to confirm taken.",
                "2 hours ago",
                "REMINDER",
                true,
                null
        ));

        list.add(new NotificationModel(
                "N4",
                "Hospital Takeaway Ready 🏥",
                "Order #102 is packed and ready for pickup at MedPlus Wellness counter #2. Have your QR code ready.",
                "Yesterday",
                "ORDER",
                true,
                102L
        ));

        list.add(new NotificationModel(
                "N5",
                "Refill Reminder: Chronic Care 🩺",
                "You have 4 days of Pantoprazole remaining. Tap to re-order with 1-click refill.",
                "2 days ago",
                "REMINDER",
                true,
                null
        ));

        return list;
    }
}