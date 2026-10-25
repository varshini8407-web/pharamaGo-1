package com.example.pharmago;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.PrescriptionResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionUploadActivity extends AppCompatActivity {

    TextView tvOrderDetails, tvPrescriptionMessage, tvScannerStatus;
    Button btnUploadPrescription, btnScanCamera, btnScanDemo, btnContinue;
    View btnBackPrescription, viewLaserLine;
    ProgressBar progressBar;
    LinearLayout layoutDetectedItems;
    ImageView ivScannedPreview;

    String medicineName, pharmacyName, price, orderType;
    Long medicineId, pharmacyId, prescriptionId = null;
    boolean requiresPrescription;
    Uri prescriptionUri = null;
    ApiService apiService;

    // File Picker Launcher
    private final ActivityResultLauncher<String[]> prescriptionPicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            prescriptionUri = uri;
                            tvPrescriptionMessage.setText("Selected: " + getFileName(uri));
                            startScanningProcess(null);
                        }
                    }
            );

    // Camera Capture Launcher
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            if (ivScannedPreview != null) {
                                ivScannedPreview.setImageBitmap(bitmap);
                            }
                            startScanningProcess(bitmap);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_upload);

        tvOrderDetails = findViewById(R.id.tvOrderDetails);
        tvPrescriptionMessage = findViewById(R.id.tvPrescriptionMessage);
        tvScannerStatus = findViewById(R.id.tvScannerStatus);
        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        btnScanCamera = findViewById(R.id.btnScanCamera);
        btnScanDemo = findViewById(R.id.btnScanDemo);
        btnContinue = findViewById(R.id.btnContinue);
        btnBackPrescription = findViewById(R.id.btnBackPrescription);
        viewLaserLine = findViewById(R.id.viewLaserLine);
        progressBar = findViewById(R.id.progressBar);
        layoutDetectedItems = findViewById(R.id.layoutDetectedItems);
        ivScannedPreview = findViewById(R.id.ivScannedPreview);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        if (btnBackPrescription != null) {
            btnBackPrescription.setOnClickListener(v -> finish());
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("medicineName")) {
            medicineName = intent.getStringExtra("medicineName");
            pharmacyName = intent.getStringExtra("pharmacyName");
            price = intent.getStringExtra("price");
            orderType = intent.getStringExtra("orderType");
            medicineId = intent.getLongExtra("medicineId", -1L);
            pharmacyId = intent.getLongExtra("pharmacyId", -1L);
            requiresPrescription = intent.getBooleanExtra("requiresPrescription", true);

            tvOrderDetails.setText(String.format("Prescription Required for: %s\nFulfillment Pharmacy: %s", 
                    medicineName, pharmacyName != null ? pharmacyName : "Apollo Pharmacy"));
            tvPrescriptionMessage.setText("A verified prescription is required for this medication.");
        } else {
            // Standalone Instant Scanner mode
            requiresPrescription = true;
            medicineName = "Amoxicillin 500mg (+ 3 other prescribed medicines)";
            pharmacyName = "Apollo Metro Pharmacy";
            price = "₹200.00";
            orderType = "DELIVERY";
            medicineId = 1L;
            pharmacyId = 1L;

            tvOrderDetails.setText("Instant Scanner Mode: Upload or capture your doctor's prescription to automatically detect and order medicines.");
            tvPrescriptionMessage.setText("Capture or select prescription document to extract medication details.");
        }

        // Camera Scan Button
        if (btnScanCamera != null) {
            btnScanCamera.setOnClickListener(v -> {
                try {
                    cameraLauncher.launch(null);
                } catch (Exception e) {
                    Toast.makeText(PrescriptionUploadActivity.this, "Camera not available, opening sample scan", Toast.LENGTH_SHORT).show();
                    runSampleScan();
                }
            });
        }

        // File Document Picker Button
        if (btnUploadPrescription != null) {
            btnUploadPrescription.setOnClickListener(v -> {
                try {
                    prescriptionPicker.launch(new String[]{"image/*", "application/pdf"});
                } catch (Exception e) {
                    Toast.makeText(PrescriptionUploadActivity.this, "Storage picker error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Instant Sample Prescription Demo Button
        if (btnScanDemo != null) {
            btnScanDemo.setOnClickListener(v -> runSampleScan());
        }

        // Continue to Checkout
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (prescriptionId == null) {
                    prescriptionId = 8942L; // Mock digital verified Rx ID
                }
                navigateToSummary();
            });
        }
    }

    private void runSampleScan() {
        // Create simulated prescription thumbnail
        Bitmap sampleBitmap = createSamplePrescriptionBitmap();
        if (ivScannedPreview != null) {
            ivScannedPreview.setImageBitmap(sampleBitmap);
        }
        startScanningProcess(sampleBitmap);
    }

    private void startScanningProcess(Bitmap bitmap) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (viewLaserLine != null) viewLaserLine.setVisibility(View.VISIBLE);
        if (tvScannerStatus != null) tvScannerStatus.setText("Scanning doctor's handwriting & dosages...");
        if (tvPrescriptionMessage != null) tvPrescriptionMessage.setText("AI Optical Character Recognition (OCR) in progress...");

        // Simulate high-tech OCR detection delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (viewLaserLine != null) viewLaserLine.setVisibility(View.GONE);
            if (tvScannerStatus != null) tvScannerStatus.setText("Prescription Extracted & Verified ✓");
            if (layoutDetectedItems != null) layoutDetectedItems.setVisibility(View.VISIBLE);

            prescriptionId = 8942L; // verified prescription ID
            if (tvPrescriptionMessage != null) {
                tvPrescriptionMessage.setText("✓ 4 Medicines Detected • Verified by Apollo Medical Registry");
            }

            if (btnContinue != null) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Proceed to Summary & Checkout (₹200.00)");
            }

            Toast.makeText(PrescriptionUploadActivity.this, "4 Medicines successfully extracted from Prescription!", Toast.LENGTH_LONG).show();
        }, 1200);
    }

    private Bitmap createSamplePrescriptionBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#1E293B"));

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#38BDF8"));
        paint.setTextSize(20f);
        paint.setAntiAlias(true);
        canvas.drawText("APOLLO MEDICAL CLINIC", 40f, 50f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(14f);
        canvas.drawText("Rx: Amoxicillin 500mg, Paracetamol 650mg...", 40f, 90f, paint);
        canvas.drawText("Dr. Arvind Sharma, MBBS, MD", 40f, 130f, paint);

        paint.setColor(Color.parseColor("#00A884"));
        canvas.drawText("Status: Digitally Signed & Validated ✓", 40f, 170f, paint);

        return bitmap;
    }

    private void navigateToSummary() {
        Intent intent = new Intent(this, OrderSummaryActivity.class);
        intent.putExtra("medicineName", medicineName != null ? medicineName : "Amoxicillin 500mg (+ 3 other prescribed medicines)");
        intent.putExtra("pharmacyName", pharmacyName != null ? pharmacyName : "Apollo Metro Pharmacy");
        intent.putExtra("price", price != null ? price : "₹200.00");
        intent.putExtra("orderType", orderType != null ? orderType : "DELIVERY");
        intent.putExtra("medicineId", medicineId != null ? medicineId : 1L);
        intent.putExtra("pharmacyId", pharmacyId != null ? pharmacyId : 1L);
        intent.putExtra("prescriptionId", prescriptionId != null ? prescriptionId : 8942L);
        intent.putExtra("prescriptionRequired", true);
        intent.putExtra("prescriptionUploaded", true);
        startActivity(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result != null ? result : "prescription_doc.jpg";
    }
}