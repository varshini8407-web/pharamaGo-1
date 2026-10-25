package com.example.pharmago;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pharmago.adapters.PharmacistOrderAdapter;
import com.example.pharmago.adapters.PrescriptionMedicineAdapter;
import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.models.Invoice;
import com.example.pharmago.models.OrderModel;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.models.Payment;
import com.example.pharmago.models.PrescriptionMedicineItem;
import com.example.pharmago.utils.NotificationManager;
import com.example.pharmago.utils.PharmacistNavigationHelper;
import com.example.pharmago.utils.QRCodeGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PharmacistOrdersActivity extends AppCompatActivity {

    private ImageView btnBackOrders, btnRefreshOrdersList;
    private TextView tvOrdersStaffBadge;
    private TabLayout tabLayoutOrders;
    private ChipGroup chipGroupOrdersFilter;
    private Chip chipFilterAll, chipFilterTakeaway, chipFilterDelivery;
    private SwipeRefreshLayout swipeRefreshOrders;
    private RecyclerView rvPharmacistOrdersFeed;
    private View layoutEmptyOrdersState;
    private ProgressBar pbOrdersLoading;
    private BottomNavigationView pharmacistBottomNav;

    private PharmacistOrderAdapter orderAdapter;
    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String staffId = "ITA1024";
    private int selectedTabPosition = 0; // 0 = Rx Required, 1 = Direct Orders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist_orders);

        databaseConnection = DatabaseConnection.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "ITA1024");
        if (savedUsername != null && savedUsername.toUpperCase().startsWith("ITA")) {
            staffId = savedUsername.toUpperCase();
        } else {
            staffId = "ITA1024";
        }

        initViews();
        setupListeners();
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pharmacistBottomNav != null) {
            pharmacistBottomNav.setSelectedItemId(R.id.nav_pharm_orders);
        }
        loadOrders();
    }

    private void initViews() {
        btnBackOrders = findViewById(R.id.btnBackOrders);
        btnRefreshOrdersList = findViewById(R.id.btnRefreshOrdersList);
        tvOrdersStaffBadge = findViewById(R.id.tvOrdersStaffBadge);

        tabLayoutOrders = findViewById(R.id.tabLayoutOrders);
        chipGroupOrdersFilter = findViewById(R.id.chipGroupOrdersFilter);
        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterTakeaway = findViewById(R.id.chipFilterTakeaway);
        chipFilterDelivery = findViewById(R.id.chipFilterDelivery);

        swipeRefreshOrders = findViewById(R.id.swipeRefreshOrders);
        rvPharmacistOrdersFeed = findViewById(R.id.rvPharmacistOrdersFeed);
        layoutEmptyOrdersState = findViewById(R.id.layoutEmptyOrdersState);
        pbOrdersLoading = findViewById(R.id.pbOrdersLoading);
        pharmacistBottomNav = findViewById(R.id.pharmacistBottomNav);

        tvOrdersStaffBadge.setText("Staff Station: " + staffId);

        rvPharmacistOrdersFeed.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new PharmacistOrderAdapter(this, new ArrayList<>(), this::showOrderVerificationDialog);
        rvPharmacistOrdersFeed.setAdapter(orderAdapter);

        PharmacistNavigationHelper.setupBottomNavigation(this, pharmacistBottomNav, R.id.nav_pharm_orders);
    }

    private void setupListeners() {
        btnBackOrders.setOnClickListener(v -> finish());
        btnRefreshOrdersList.setOnClickListener(v -> loadOrders());
        swipeRefreshOrders.setOnRefreshListener(this::loadOrders);

        tabLayoutOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabPosition = tab.getPosition();
                loadOrders();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroupOrdersFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipFilterTakeaway)) {
                orderAdapter.filterOrders("TAKEAWAY");
            } else if (checkedIds.contains(R.id.chipFilterDelivery)) {
                orderAdapter.filterOrders("DELIVERY");
            } else {
                orderAdapter.filterOrders("ALL");
            }
            updateEmptyState();
        });
    }

    private void loadOrders() {
        if (!swipeRefreshOrders.isRefreshing()) {
            pbOrdersLoading.setVisibility(View.VISIBLE);
        }

        executorService.execute(() -> {
            String tabFilter = (selectedTabPosition == 0) ? "PRESCRIPTION" : "DIRECT";
            List<OrderModel> orders = databaseConnection.getPharmacistOrders(tabFilter);

            mainHandler.post(() -> {
                pbOrdersLoading.setVisibility(View.GONE);
                swipeRefreshOrders.setRefreshing(false);
                orderAdapter.updateData(orders);

                // Apply active chip
                if (chipFilterTakeaway.isChecked()) {
                    orderAdapter.filterOrders("TAKEAWAY");
                } else if (chipFilterDelivery.isChecked()) {
                    orderAdapter.filterOrders("DELIVERY");
                } else {
                    orderAdapter.filterOrders("ALL");
                }
                updateEmptyState();
            });
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = orderAdapter.getItemCount() == 0;
        layoutEmptyOrdersState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPharmacistOrdersFeed.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Requirement 3: Prescription Verification Dialog
     */
    private void showOrderVerificationDialog(OrderModel order) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pharmacist_order, null);
        dialog.setContentView(dialogView);

        TextView tvDialogOrderId = dialogView.findViewById(R.id.tvDialogOrderId);
        TextView tvDialogPatientInfo = dialogView.findViewById(R.id.tvDialogPatientInfo);
        TextView tvDialogOrderType = dialogView.findViewById(R.id.tvDialogOrderTypeBadge);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        View cardPrescriptionPreview = dialogView.findViewById(R.id.cardPrescriptionPreview);

        RecyclerView rvPrescriptionItems = dialogView.findViewById(R.id.rvPrescriptionMedicines);
        TextView tvBillSubtotal = dialogView.findViewById(R.id.tvBillSubtotal);
        TextView tvBillPlatformFee = dialogView.findViewById(R.id.tvBillPlatformFee);
        TextView tvBillTax = dialogView.findViewById(R.id.tvBillTax);
        TextView tvBillGrandTotal = dialogView.findViewById(R.id.tvBillGrandTotal);

        MaterialButton btnApproveOrder = dialogView.findViewById(R.id.btnApproveOrder);
        MaterialButton btnRejectOrder = dialogView.findViewById(R.id.btnRejectOrder);

        if (tvDialogOrderId != null) tvDialogOrderId.setText("Order #" + order.getOrderId() + " Verification");
        if (tvDialogPatientInfo != null) tvDialogPatientInfo.setText("Patient: " + order.getPatientName() + " • " + order.getPatientPhone());
        if (tvDialogOrderType != null) tvDialogOrderType.setText(order.getOrderType());

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        View.OnClickListener inspectRxListener = v -> {
            View imageModal = LayoutInflater.from(this).inflate(R.layout.dialog_prescription_image_viewer, null);
            TextView tvModalTitle = imageModal.findViewById(R.id.tvModalRxTitle);
            if (tvModalTitle != null) tvModalTitle.setText("Rx #" + order.getOrderId() + " - " + order.getPatientName());

            new MaterialAlertDialogBuilder(this)
                    .setView(imageModal)
                    .setPositiveButton("Close", null)
                    .show();
        };

        if (cardPrescriptionPreview != null) cardPrescriptionPreview.setOnClickListener(inspectRxListener);

        List<PrescriptionMedicineItem> items = new ArrayList<>(order.getMedicineList());
        if (rvPrescriptionItems != null) {
            rvPrescriptionItems.setLayoutManager(new LinearLayoutManager(this));

            PrescriptionMedicineAdapter medAdapter = new PrescriptionMedicineAdapter(this, items, () -> {
                calculateAndDisplayBill(items, tvBillSubtotal, tvBillPlatformFee, tvBillTax, tvBillGrandTotal);
            });
            rvPrescriptionItems.setAdapter(medAdapter);
        }

        calculateAndDisplayBill(items, tvBillSubtotal, tvBillPlatformFee, tvBillTax, tvBillGrandTotal);

        // Approve Order Button
        btnApproveOrder.setOnClickListener(v -> {
            executeOrderApprovalTransaction(dialog, order, items);
        });

        // Reject Order Button
        btnRejectOrder.setOnClickListener(v -> {
            showRejectionDialog(dialog, order);
        });

        dialog.show();
    }

    private void calculateAndDisplayBill(List<PrescriptionMedicineItem> items, TextView tvSubtotal,
                                         TextView tvPlatformFee, TextView tvTax, TextView tvGrandTotal) {
        double subtotal = 0.0;
        for (PrescriptionMedicineItem item : items) {
            subtotal += item.getSubtotal();
        }

        double platformFee = 15.00;
        double tax = subtotal * 0.05;
        double grandTotal = subtotal + platformFee + tax;

        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        tvPlatformFee.setText(String.format(Locale.getDefault(), "₹%.2f", platformFee));
        tvTax.setText(String.format(Locale.getDefault(), "₹%.2f", tax));
        tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));
    }

    private void executeOrderApprovalTransaction(BottomSheetDialog dialog, OrderModel order, List<PrescriptionMedicineItem> items) {
        double subtotal = 0.0;
        int totalUnits = 0;
        for (PrescriptionMedicineItem item : items) {
            subtotal += item.getSubtotal();
            totalUnits += item.getRequestedQty();
        }

        if (totalUnits == 0) {
            Toast.makeText(this, "Cannot approve order with 0 medicine quantities", Toast.LENGTH_SHORT).show();
            return;
        }

        double platformFee = 15.00;
        double tax = subtotal * 0.05;
        double grandTotal = subtotal + platformFee + tax;
        final double finalSubtotal = subtotal;
        final double finalTax = tax;
        final double finalGrandTotal = grandTotal;
        long invoiceId = System.currentTimeMillis() % 1000000;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        boolean isTakeaway = "TAKEAWAY".equalsIgnoreCase(order.getOrderType());

        // Requirement 2 & 3: Database transaction via BackendService
        com.example.pharmago.services.BackendService.getInstance(this).approveAndGenerateInvoice(
                order.getOrderId(),
                order.getOrderType(),
                items,
                finalSubtotal,
                finalTax,
                finalGrandTotal,
                null,
                new com.example.pharmago.database.DbCallback<com.example.pharmago.models.OrderApprovalResult>() {
                    @Override
                    public void onSuccess(com.example.pharmago.models.OrderApprovalResult result) {
                        OrderStatus newStatus = isTakeaway ? OrderStatus.READY_FOR_PICKUP : OrderStatus.READY_FOR_DELIVERY;
                        order.setStatus(newStatus);
                        order.setInvoiceId(result.getInvoiceId());
                        order.setTotalAmount(result.getAmount());

                        Bitmap qrBitmap = null;
                        byte[] qrBytes = null;
                        if (isTakeaway && result.getQrString() != null) {
                            order.setQrCodeString(result.getQrString());
                            qrBitmap = com.example.pharmago.utils.QRCodeGenerator.generateQRCodeBitmap(result.getQrString(), 500);
                            qrBytes = com.example.pharmago.utils.QRCodeGenerator.generateQRCodeBytes(result.getQrString(), 500);
                            order.setQrCodeBytes(qrBytes);
                        } else if (!isTakeaway && result.getOtpCode() != null) {
                            order.setOtp(result.getOtpCode());
                        }

                        // Local SQLite sync
                        databaseConnection.updateOrderApproval(
                                order.getOrderId(),
                                newStatus,
                                result.getOtpCode(),
                                result.getQrString(),
                                qrBytes,
                                result.getInvoiceId(),
                                result.getAmount()
                        );

                        com.example.pharmago.utils.NotificationManager.notifyPatientOrderVerified(PharmacistOrdersActivity.this, order);

                        dialog.dismiss();
                        loadOrders();

                        if (isTakeaway) {
                            showTakeawayApprovalSuccessDialog(order, result.getInvoiceId(), result.getQrString(), qrBitmap);
                        } else {
                            showDeliveryApprovalSuccessDialog(order, result.getInvoiceId(), result.getOtpCode());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PharmacistOrdersActivity.this, "Approval failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void showRejectionDialog(BottomSheetDialog parentDialog, OrderModel order) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reject_order, null);
        BottomSheetDialog rejectDialog = new BottomSheetDialog(this);
        rejectDialog.setContentView(view);

        RadioGroup rg = view.findViewById(R.id.rgRejectionReasons);
        TextInputEditText etRemarks = view.findViewById(R.id.etRejectionCustomRemarks);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelRejection);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmRejection);

        btnCancel.setOnClickListener(v -> rejectDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int selectedRadioId = rg.getCheckedRadioButtonId();
            RadioButton selectedRb = view.findViewById(selectedRadioId);
            String reason = selectedRb != null ? selectedRb.getText().toString() : "Prescription verification declined";

            String customRemarks = etRemarks.getText() != null ? etRemarks.getText().toString().trim() : "";
            if (!customRemarks.isEmpty()) {
                reason += " (" + customRemarks + ")";
            }

            final String finalReason = reason;
            btnConfirm.setEnabled(false);

            executorService.execute(() -> {
                boolean success = databaseConnection.rejectOrder(order.getOrderId(), finalReason);
                mainHandler.post(() -> {
                    rejectDialog.dismiss();
                    parentDialog.dismiss();
                    loadOrders();

                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Order Rejected")
                            .setMessage("Order #" + order.getOrderId() + " was rejected.\n\nReason: " + finalReason + "\n\nThe patient has been notified.")
                            .setPositiveButton("OK", null)
                            .show();
                });
            });
        });

        rejectDialog.show();
    }

    private void showTakeawayApprovalSuccessDialog(OrderModel order, long invoiceId, String qrString, Bitmap qrBitmap) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_pickup_qr, null);

        TextView tvPickupStatus = dialogView.findViewById(R.id.tvPickupStatus);
        TextView tvPickupDetails = dialogView.findViewById(R.id.tvPickupDetails);
        ImageView ivQrCode = dialogView.findViewById(R.id.imgPickupQR);

        if (tvPickupStatus != null) tvPickupStatus.setText("Takeaway Ready • Invoice #" + invoiceId);
        if (tvPickupDetails != null) tvPickupDetails.setText("Patient: " + order.getPatientName() + " • Token: " + qrString);
        if (ivQrCode != null && qrBitmap != null) ivQrCode.setImageBitmap(qrBitmap);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Order Approved (Takeaway)")
                .setMessage("Inventory deducted. Scannable pickup QR code generated for patient counter verification.")
                .setView(dialogView)
                .setPositiveButton("Done", (d, w) -> d.dismiss())
                .show();
    }

    private void showDeliveryApprovalSuccessDialog(OrderModel order, long invoiceId, String otp) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Order Approved (Delivery)")
                .setMessage("Invoice #" + invoiceId + " generated.\n\n" +
                        "• Inventory stock deducted\n" +
                        "• Delivery Handover OTP: " + otp + "\n" +
                        "• Patient and delivery courier notified.")
                .setPositiveButton("Done", (d, w) -> d.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
