package com.example.pharmago;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.OrderResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickupQRActivity extends AppCompatActivity {

    TextView tvPickupStatus, tvPickupDetails, tvPickupQR;
    ImageView imgPickupQR;
    Button btnBackHome;
    ProgressBar progressBar;
    ApiService apiService;
    Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_qr);

        tvPickupStatus = findViewById(R.id.tvPickupStatus);
        tvPickupDetails = findViewById(R.id.tvPickupDetails);
        tvPickupQR = findViewById(R.id.tvPickupQR);
        imgPickupQR = findViewById(R.id.imgPickupQR);
        btnBackHome = findViewById(R.id.btnBackHome);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        View btnBackPickup = findViewById(R.id.btnBackPickup);
        if (btnBackPickup != null) {
            btnBackPickup.setOnClickListener(v -> finish());
        }

        Intent intent = getIntent();
        String medicineName = intent.getStringExtra("medicineName");
        String pharmacyName = intent.getStringExtra("pharmacyName");
        String price = intent.getStringExtra("price");
        String status = intent.getStringExtra("status");
        orderId = intent.getLongExtra("orderId", 102L);

        tvPickupStatus.setText(status != null ? "Status: " + status : "Ready for Hospital Pickup");
        
        if (pharmacyName == null) pharmacyName = "MedPlus Wellness (Ground Floor Counter)";
        if (price == null) price = "₹120.00";
        if (medicineName == null) medicineName = "Amoxicillin 250mg Capsules";

        tvPickupDetails.setText(String.format("Pharmacy: %s\nItem: %s\nTotal: %s\nStatus: Verified ✓\n\nShow this QR token to the pharmacist at pickup counter.",
                pharmacyName, medicineName, price));

        long currentOrderId = (orderId != null && orderId != -1L) ? orderId : 102L;
        long invoiceId = intent.getLongExtra("invoiceId", currentOrderId + 1000L);
        String passedPayload = intent.getStringExtra("pickup_qr_payload");
        if (passedPayload == null) passedPayload = intent.getStringExtra("pickupQrPayload");
        String qrToken = (passedPayload != null && !passedPayload.trim().isEmpty())
                ? passedPayload.trim()
                : com.example.pharmago.utils.QRCodeGenerator.formatTakeawayPayload(currentOrderId, invoiceId);
        tvPickupQR.setText(String.format("Pass: ORDER #%d • Token: %s", currentOrderId, qrToken));
        generateQRCodeLocally(qrToken);

        if (orderId != -1L) {
            fetchOrderDetails();
            fetchQRCodeFromServer(orderId);
        }

        btnBackHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(PickupQRActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(homeIntent);
            finish();
        });
    }

    private void fetchOrderDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        apiService.getOrderDetails(orderId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse order = response.body();
                    tvPickupDetails.setText(String.format("Pharmacy: %s\nTotal: ₹%s\nStatus: %s\n\nShow your pickup QR code to the pharmacist.",
                            order.getPharmacyName(), order.getTotalPrice(), order.getStatus()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchQRCodeFromServer(Long orderId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        tvPickupQR.setText(R.string.fetching_qr);

        apiService.getPickupQrCode(orderId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().containsKey("qrCode")) {
                    String base64Qr = response.body().get("qrCode");
                    displayBase64Qr(base64Qr);
                    tvPickupQR.setText(getString(R.string.pickup_id_format, String.valueOf(orderId)));
                } else {
                    Toast.makeText(PickupQRActivity.this, R.string.qr_fetch_failed, Toast.LENGTH_SHORT).show();
                    long fallbackId = orderId != null ? orderId : 101L;
                    String fallbackToken = "ORDER:" + fallbackId + ":INV:" + (fallbackId + 1000L);
                    generateQRCodeLocally(fallbackToken);
                    tvPickupQR.setText(String.format("Pass: ORDER #%d • INV #%d", fallbackId, fallbackId + 1000L));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                long fallbackId = orderId != null ? orderId : 101L;
                String fallbackToken = "ORDER:" + fallbackId + ":INV:" + (fallbackId + 1000L);
                generateQRCodeLocally(fallbackToken);
                tvPickupQR.setText(String.format("Pass: ORDER #%d • INV #%d", fallbackId, fallbackId + 1000L));
            }
        });
    }

    private void displayBase64Qr(String base64String) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imgPickupQR.setImageBitmap(decodedByte);
        } catch (Exception e) {
            generateQRCodeLocally("Error Displaying QR");
        }
    }

    private void generateQRCodeLocally(String data) {
        Bitmap bitmap = com.example.pharmago.utils.QRCodeGenerator.generateQRCode(data, 500, 500);
        if (bitmap != null) {
            imgPickupQR.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Local QR generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}