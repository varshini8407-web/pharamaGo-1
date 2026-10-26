package com.example.pharmago;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.DeliveryResponse;
import com.example.pharmago.views.DeliveryMapView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.widget.EditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DeliveryTrackingActivity extends AppCompatActivity {

    private TextView tvPharmacyTitle, tvDeliveryStatus, tvEtaBadge, tvMapOrderBadge;
    private TextView tvDriverName, tvDeliveryDetails;
    private TextView tvOrderConfirmed, tvPreparing, tvOutForDelivery, tvDelivered;
    private TextView btnChatNowAlert, tvAlertMessageText;
    private View btnDismissAlert;
    private ImageView btnBackDelivery, btnShareDelivery, btnRefreshTracking;
    private View btnChatDriver, btnCallDriver;
    private Button btnSimulateDelivered, btnDeliveryHome;
    private LinearLayout layoutAlertMessage;
    private DeliveryMapView deliveryMapView;
    private ProgressBar progressBar;

    private Long orderId;
    private String passedPharmacyName, passedPrice, passedStatus, passedAddress;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_tracking);

        // Bind all UI views
        btnBackDelivery = findViewById(R.id.btnBackDelivery);
        tvPharmacyTitle = findViewById(R.id.tvPharmacyTitle);
        btnShareDelivery = findViewById(R.id.btnShareDelivery);
        tvDeliveryStatus = findViewById(R.id.tvDeliveryStatus);
        tvEtaBadge = findViewById(R.id.tvEtaBadge);
        btnRefreshTracking = findViewById(R.id.btnRefreshTracking);

        layoutAlertMessage = findViewById(R.id.layoutAlertMessage);
        tvAlertMessageText = findViewById(R.id.tvAlertMessageText);
        btnChatNowAlert = findViewById(R.id.btnChatNowAlert);
        btnDismissAlert = findViewById(R.id.btnDismissAlert);

        if (tvAlertMessageText != null) {
            tvAlertMessageText.setText(android.text.Html.fromHtml("You have <b>1 new message</b> from the<br/>delivery partner", android.text.Html.FROM_HTML_MODE_LEGACY));
        }

        deliveryMapView = findViewById(R.id.deliveryMapView);
        tvMapOrderBadge = findViewById(R.id.tvMapOrderBadge);
        tvDriverName = findViewById(R.id.tvDriverName);
        btnChatDriver = findViewById(R.id.btnChatDriver);
        btnCallDriver = findViewById(R.id.btnCallDriver);

        tvDeliveryDetails = findViewById(R.id.tvDeliveryDetails);
        tvOrderConfirmed = findViewById(R.id.tvOrderConfirmed);
        tvPreparing = findViewById(R.id.tvPreparing);
        tvOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvDelivered = findViewById(R.id.tvDelivered);

        btnSimulateDelivered = findViewById(R.id.btnSimulateDelivered);
        btnDeliveryHome = findViewById(R.id.btnDeliveryHome);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        Intent intent = getIntent();
        orderId = intent.getLongExtra("orderId", 103L);
        passedPharmacyName = intent.getStringExtra("pharmacyName");
        passedPrice = intent.getStringExtra("price");
        passedStatus = intent.getStringExtra("status");
        passedAddress = intent.getStringExtra("deliveryAddress");

        if (passedPharmacyName == null) passedPharmacyName = "Apollo Metro Pharmacy";
        if (passedPrice == null) passedPrice = "₹90.00";
        if (passedStatus == null) passedStatus = "IN TRANSIT";
        if (passedAddress == null) passedAddress = "Apt 402, Green Avenue, Sector 4, Metro City";

        if (tvPharmacyTitle != null) {
            tvPharmacyTitle.setText(passedPharmacyName);
        }
        if (tvMapOrderBadge != null) {
            tvMapOrderBadge.setText(String.format(Locale.getDefault(), "Verified Rx • Order #%d", orderId));
        }

        applyTrackingState(passedStatus);

        TextView tvTrackingOtpCode = findViewById(R.id.tvTrackingOtpCode);
        if (tvTrackingOtpCode != null) {
            String passedOtp = intent.getStringExtra("deliveryOtp");
            if (passedOtp != null && !passedOtp.trim().isEmpty()) {
                String clean = passedOtp.trim();
                if (clean.length() == 6) {
                    tvTrackingOtpCode.setText(String.format("[ %c %c %c %c %c %c ]",
                            clean.charAt(0), clean.charAt(1), clean.charAt(2),
                            clean.charAt(3), clean.charAt(4), clean.charAt(5)));
                } else {
                    tvTrackingOtpCode.setText("[ " + clean + " ]");
                }
            } else {
                long orderNum = orderId != null ? orderId : 103L;
                int digit1 = (int) ((orderNum * 7 + 1) % 9) + 1;
                int digit2 = (int) ((orderNum * 3 + 2) % 9) + 1;
                int digit3 = (int) ((orderNum * 5 + 4) % 9) + 1;
                int digit4 = (int) ((orderNum * 2 + 5) % 9) + 1;
                int digit5 = (int) ((orderNum * 8 + 3) % 9) + 1;
                int digit6 = (int) ((orderNum * 4 + 7) % 9) + 1;
                tvTrackingOtpCode.setText(String.format(Locale.getDefault(), "[ %d %d %d %d %d %d ]", digit1, digit2, digit3, digit4, digit5, digit6));
            }

            com.example.pharmago.services.BackendService.getInstance(this).getOrderDeliveryOtp(orderId != null ? orderId : 103L, new com.example.pharmago.database.DbCallback<String>() {
                @Override
                public void onSuccess(String liveOtp) {
                    if (liveOtp != null && !liveOtp.trim().isEmpty()) {
                        String clean = liveOtp.trim();
                        if (clean.length() == 6) {
                            tvTrackingOtpCode.setText(String.format("[ %c %c %c %c %c %c ]",
                                    clean.charAt(0), clean.charAt(1), clean.charAt(2),
                                    clean.charAt(3), clean.charAt(4), clean.charAt(5)));
                        } else {
                            tvTrackingOtpCode.setText("[ " + clean + " ]");
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {}
            });
        }

        if (btnBackDelivery != null) {
            btnBackDelivery.setOnClickListener(v -> finish());
        }

        if (btnShareDelivery != null) {
            btnShareDelivery.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Track My PharmaGo Order");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Track my healthcare delivery in real-time from " + passedPharmacyName + " (Order #" + orderId + "): https://pharmago.com/track/" + orderId);
                startActivity(Intent.createChooser(shareIntent, "Share Live Tracking"));
            });
        }

        if (btnRefreshTracking != null) {
            btnRefreshTracking.setOnClickListener(v -> {
                if (deliveryMapView != null) {
                    deliveryMapView.animateScooterProgress(0.85f);
                }
                if (tvEtaBadge != null) {
                    tvEtaBadge.setText("Arriving in 4 mins");
                }
                Toast.makeText(this, "Refreshed partner location (4 mins away)", Toast.LENGTH_SHORT).show();
            });
        }

        // Chat actions
        View.OnClickListener chatClickListener = v -> showChatDialog();
        if (btnChatNowAlert != null) btnChatNowAlert.setOnClickListener(chatClickListener);
        if (btnChatDriver != null) btnChatDriver.setOnClickListener(chatClickListener);

        if (btnDismissAlert != null && layoutAlertMessage != null) {
            btnDismissAlert.setOnClickListener(v -> layoutAlertMessage.setVisibility(View.GONE));
        }

        // Call Driver Action
        if (btnCallDriver != null) {
            btnCallDriver.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"));
                startActivity(dialIntent);
            });
        }

        // Simulation Button: Mark as Delivered
        if (btnSimulateDelivered != null) {
            btnSimulateDelivered.setOnClickListener(v -> {
                if (deliveryMapView != null) {
                    deliveryMapView.animateScooterProgress(1.0f);
                }
                applyTrackingState("DELIVERED");
                Toast.makeText(this, "Order marked as Delivered! ✓", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnDeliveryHome != null) {
            btnDeliveryHome.setOnClickListener(v -> {
                Intent homeIntent = new Intent(DeliveryTrackingActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                finish();
            });
        }

        // Attempt background fetch safely
        fetchTrackingFromServer();
    }

    private void applyTrackingState(String status) {
        String upper = status != null ? status.toUpperCase() : "IN TRANSIT";

        if (upper.contains("DELIVERED")) {
            getSharedPreferences("PharmaGoOrders", MODE_PRIVATE)
                    .edit()
                    .putBoolean("hasActiveLiveOrder", false)
                    .apply();

            if (tvDeliveryStatus != null) {
                tvDeliveryStatus.setText("Order Delivered Successfully! 🎉");
            }
            if (tvEtaBadge != null) {
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                tvEtaBadge.setText("Delivered at " + timeStr);
            }
            markActive(tvOrderConfirmed, "✓ Order Confirmed at Pharmacy");
            markActive(tvPreparing, "✓ Medicines Sealed & Picked Up");
            markActive(tvOutForDelivery, "✓ Out for Delivery with Partner");
            markActive(tvDelivered, "✓ Delivered to Customer");
        } else {
            if (tvDeliveryStatus != null) {
                tvDeliveryStatus.setText("Order is on the way 🤘");
            }
            if (tvEtaBadge != null) {
                tvEtaBadge.setText("Arriving in 7 mins");
            }
            markActive(tvOrderConfirmed, "✓ Order Confirmed at Pharmacy");
            markActive(tvPreparing, "✓ Medicines Sealed & Handed to Courier");
            markCurrent(tvOutForDelivery, "● Out for Delivery with Chandan (7 mins away)");
            resetStep(tvDelivered, "○ Delivered to Customer");
        }

        if (tvDeliveryDetails != null) {
            tvDeliveryDetails.setText(String.format("Pharmacy: %s\nDestination: %s\nTotal Amount: %s\nVerification: Sealed & Tamper-Proof ✓",
                    passedPharmacyName, passedAddress, passedPrice));
        }
    }

    private void showChatDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_courier_chat, null);
        dialog.setContentView(dialogView);

        View btnClose = dialogView.findViewById(R.id.btnCloseChatDialog);
        LinearLayout layoutReplies = dialogView.findViewById(R.id.layoutChatReplies);
        EditText etChat = dialogView.findViewById(R.id.etChatMessage);
        View btnSend = dialogView.findViewById(R.id.btnSendChatMessage);

        View chipHome = dialogView.findViewById(R.id.chipReplyHome);
        View chipDoor = dialogView.findViewById(R.id.chipReplyDoor);
        View chipCall = dialogView.findViewById(R.id.chipReplyCall);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        View.OnClickListener replyClickListener = v -> {
            String msg = "";
            if (v == chipHome) msg = "I am at home 👍";
            else if (v == chipDoor) msg = "Please leave at door 🚪";
            else if (v == chipCall) msg = "Call me when you reach 📞";
            else if (v == btnSend && etChat != null && etChat.getText() != null) {
                msg = etChat.getText().toString().trim();
                etChat.setText("");
            }

            if (!msg.isEmpty()) {
                addOutgoingBubble(layoutReplies, msg);
                Toast.makeText(DeliveryTrackingActivity.this, "Message sent to Chandan ✓", Toast.LENGTH_SHORT).show();
            }
        };

        if (chipHome != null) chipHome.setOnClickListener(replyClickListener);
        if (chipDoor != null) chipDoor.setOnClickListener(replyClickListener);
        if (chipCall != null) chipCall.setOnClickListener(replyClickListener);
        if (btnSend != null) btnSend.setOnClickListener(replyClickListener);

        dialog.show();
    }

    private void addOutgoingBubble(LinearLayout container, String text) {
        if (container == null) return;
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundResource(R.drawable.badge_bg);
        bubble.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_clinical_blue));
        bubble.setPadding(36, 20, 36, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.END;
        params.bottomMargin = 14;
        bubble.setLayoutParams(params);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(13f);
        bubble.addView(tv);

        TextView timeTv = new TextView(this);
        timeTv.setText("Sent just now • Delivered ✓");
        timeTv.setTextColor(android.graphics.Color.parseColor("#BFDBFE"));
        timeTv.setTextSize(10f);
        timeTv.setPadding(0, 4, 0, 0);
        bubble.addView(timeTv);

        container.addView(bubble);
    }

    private void fetchTrackingFromServer() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        apiService.getDeliveryTracking(orderId).enqueue(new Callback<DeliveryResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeliveryResponse> call, @NonNull Response<DeliveryResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    DeliveryResponse delivery = response.body();
                    applyTrackingState(delivery.getStatus());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeliveryResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                // Safe offline fallback: tracking state already applied
            }
        });
    }

    private void markActive(TextView view, String text) {
        if (view == null) return;
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.secondary_mint));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void markCurrent(TextView view, String text) {
        if (view == null) return;
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.primary_clinical_blue));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetStep(TextView view, String text) {
        if (view == null) return;
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.neutral_muted));
        view.setTypeface(null, android.graphics.Typeface.NORMAL);
    }
}