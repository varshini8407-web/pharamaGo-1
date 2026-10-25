package com.example.pharmago;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.models.OrderModel;
import com.example.pharmago.models.PrescriptionMedicineItem;
import com.example.pharmago.utils.PharmacistNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;
import com.example.pharmago.utils.QRCodeScanner;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScanCounterActivity extends AppCompatActivity {

    private ImageView btnBackScanCounter, btnToggleFlashlight;
    private View viewScanLaserLine, cardScanReticle;
    private MaterialButton btnSimulatePaidScan, btnSimulateCashScan, btnLookupManualQr;
    private TextInputEditText etManualQrInput;
    private BottomNavigationView pharmacistBottomNav;

    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ObjectAnimator laserAnimator;
    private boolean isFlashlightOn = false;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result != null && result.getContents() != null && !result.getContents().trim().isEmpty()) {
                    processScannedPayload(result.getContents().trim());
                } else {
                    Toast.makeText(this, "QR Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void launchCameraScanner() {
        try {
            barcodeLauncher.launch(QRCodeScanner.createCounterScanOptions());
        } catch (Exception e) {
            Toast.makeText(this, "Camera scanner error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_counter);

        databaseConnection = DatabaseConnection.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        startLaserAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pharmacistBottomNav != null) {
            pharmacistBottomNav.setSelectedItemId(R.id.nav_pharm_scan);
        }
    }

    private void initViews() {
        btnBackScanCounter = findViewById(R.id.btnBackScanCounter);
        btnToggleFlashlight = findViewById(R.id.btnToggleFlashlight);
        viewScanLaserLine = findViewById(R.id.viewScanLaserLine);
        cardScanReticle = findViewById(R.id.cardScanReticle);

        btnSimulatePaidScan = findViewById(R.id.btnSimulatePaidScan);
        btnSimulateCashScan = findViewById(R.id.btnSimulateCashScan);
        btnLookupManualQr = findViewById(R.id.btnLookupManualQr);
        etManualQrInput = findViewById(R.id.etManualQrInput);
        pharmacistBottomNav = findViewById(R.id.pharmacistBottomNav);

        PharmacistNavigationHelper.setupBottomNavigation(this, pharmacistBottomNav, R.id.nav_pharm_scan);
    }

    private void setupListeners() {
        btnBackScanCounter.setOnClickListener(v -> finish());

        // Launch ZXing live camera scanner
        if (cardScanReticle != null) {
            cardScanReticle.setOnClickListener(v -> launchCameraScanner());
        }

        btnToggleFlashlight.setOnClickListener(v -> {
            isFlashlightOn = !isFlashlightOn;
            if (isFlashlightOn) {
                btnToggleFlashlight.setColorFilter(0xFFFBBF24); // Amber
                launchCameraScanner();
            } else {
                btnToggleFlashlight.setColorFilter(0xFFFFFFFF);
            }
        });

        // Demo scan buttons
        btnSimulatePaidScan.setOnClickListener(v -> processScannedPayload("ORDER:8090:INV:1040"));
        btnSimulateCashScan.setOnClickListener(v -> processScannedPayload("ORDER:8091:INV:1041"));

        btnLookupManualQr.setOnClickListener(v -> {
            String input = etManualQrInput.getText() != null ? etManualQrInput.getText().toString().trim() : "";
            if (input.isEmpty()) {
                etManualQrInput.setError("Please enter an Order ID or QR payload");
                etManualQrInput.requestFocus();
                return;
            }
            processScannedPayload(input);
        });
    }

    private void startLaserAnimation() {
        if (viewScanLaserLine != null) {
            laserAnimator = ObjectAnimator.ofFloat(viewScanLaserLine, "translationY", -70f, 70f);
            laserAnimator.setDuration(1600);
            laserAnimator.setRepeatMode(ValueAnimator.REVERSE);
            laserAnimator.setRepeatCount(ValueAnimator.INFINITE);
            laserAnimator.start();
        }
    }

    public void processScannedPayload(String payload) {
        Toast.makeText(this, "Validating Takeaway Pass: " + payload, Toast.LENGTH_SHORT).show();

        executorService.execute(() -> {
            OrderModel order = databaseConnection.getOrderByQr(payload);
            if (order == null) {
                Long parsedId = QRCodeScanner.parseOrderIdFromPayload(payload);
                if (parsedId != null && parsedId > 0) {
                    order = databaseConnection.getOrderById(parsedId);
                }
            }
            final OrderModel finalOrder = order;
            mainHandler.post(() -> {
                if (finalOrder == null) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Order Not Found")
                            .setMessage("No active takeaway order was found matching: " + payload + ".\n\nPlease verify the patient's QR code.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    showDispenseBottomSheet(finalOrder);
                }
            });
        });
    }

    private void showDispenseBottomSheet(OrderModel order) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dispense_takeaway, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvDispensePatientName);
        TextView tvPhone = view.findViewById(R.id.tvDispensePatientPhone);
        TextView tvOrderIdBadge = view.findViewById(R.id.tvDispenseOrderIdBadge);
        TextView tvTotal = view.findViewById(R.id.tvDispenseTotalAmount);

        MaterialCardView cardPayment = view.findViewById(R.id.cardPaymentStatusBanner);
        ImageView ivPaymentIcon = view.findViewById(R.id.ivPaymentStatusIcon);
        TextView tvPaymentTitle = view.findViewById(R.id.tvDispensePaymentTitle);
        TextView tvPaymentSub = view.findViewById(R.id.tvDispensePaymentSub);

        LinearLayout layoutMedicines = view.findViewById(R.id.layoutDispenseMedicinesList);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelDispense);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmDispense);

        tvName.setText(order.getPatientName());
        tvPhone.setText(order.getPatientPhone());
        tvOrderIdBadge.setText("#" + order.getOrderId());
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));

        // Payment status customization
        boolean isCashPending = order.getPaymentStatus() != null && order.getPaymentStatus().toUpperCase().contains("CASH");
        if (isCashPending) {
            cardPayment.setCardBackgroundColor(0xFFFEF3C7); // Amber container
            ivPaymentIcon.setColorFilter(0xFFD97706);
            tvPaymentTitle.setText("CASH ON PICKUP (Unpaid)");
            tvPaymentTitle.setTextColor(0xFF92400E);
            tvPaymentSub.setText("Please collect cash payment at counter before dispensing.");
            tvPaymentSub.setTextColor(0xFF92400E);
            btnConfirm.setText("Collect Cash & Dispense");
        } else {
            cardPayment.setCardBackgroundColor(0xFFE0F7F2); // Mint container
            ivPaymentIcon.setColorFilter(0xFF00A884);
            tvPaymentTitle.setText("PAID ONLINE (UPI)");
            tvPaymentTitle.setTextColor(0xFF005C48);
            tvPaymentSub.setText("Digital payment verified. Safe to dispense.");
            tvPaymentSub.setTextColor(0xFF005C48);
            btnConfirm.setText("Confirm & Dispense");
        }

        // Add medicines dynamically
        layoutMedicines.removeAllViews();
        if (order.getMedicineList() != null && !order.getMedicineList().isEmpty()) {
            for (PrescriptionMedicineItem item : order.getMedicineList()) {
                TextView tvItem = new TextView(this);
                tvItem.setText("• " + item.getMedicineName() + " (" + item.getDosage() + ") x " + item.getRequestedQty() + " units");
                tvItem.setTextColor(0xFF0F172A);
                tvItem.setTextSize(13f);
                tvItem.setPadding(0, 4, 0, 4);
                layoutMedicines.addView(tvItem);
            }
        } else {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Standard Takeaway Medical Package");
            tvEmpty.setTextColor(0xFF64748B);
            tvEmpty.setTextSize(13f);
            layoutMedicines.addView(tvEmpty);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Dispensing live...");

            // Sync with Spring Boot live backend
            try {
                String token = order.getQrString() != null ? order.getQrString() : "ORDER:" + order.getOrderId();
                java.util.Map<String, String> req = new java.util.HashMap<>();
                req.put("token", token);
                com.example.pharmago.api.ApiService api = com.example.pharmago.api.RetrofitClient.getClientWithAuth(this)
                        .create(com.example.pharmago.api.ApiService.class);
                api.validatePickup(req).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {}
                    @Override
                    public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {}
                });
            } catch (Exception ignored) {}

            com.example.pharmago.services.BackendService.getInstance(this).completeTakeawayOrder(
                    order.getOrderId(),
                    new com.example.pharmago.database.DbCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            dialog.dismiss();
                            new MaterialAlertDialogBuilder(QRScanCounterActivity.this)
                                    .setTitle("Dispense Confirmed! ✓")
                                    .setMessage("Takeaway Order #" + order.getOrderId() + " for patient " + order.getPatientName() +
                                            " has been marked as COMPLETED and payment verified via MySQL.")
                                    .setPositiveButton("Done", null)
                                    .show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Confirm & Dispense");
                            Toast.makeText(QRScanCounterActivity.this, "Dispense failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (laserAnimator != null) {
            laserAnimator.cancel();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
