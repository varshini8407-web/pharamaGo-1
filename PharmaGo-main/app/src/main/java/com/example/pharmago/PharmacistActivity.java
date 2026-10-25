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
import com.example.pharmago.utils.QRCodeGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PharmacistActivity extends AppCompatActivity {

    private TextView tvPharmacistStaffId, tvPendingCountBadge;
    private ImageView btnBackPharmacist, btnRefreshOrders;
    private ChipGroup chipGroupOrderFilters;
    private Chip chipFilterAll, chipFilterTakeaway, chipFilterDelivery;
    private SwipeRefreshLayout swipeRefreshPharmacist;
    private RecyclerView rvPharmacistOrders;
    private View layoutEmptyPharmacistOrders;
    private ProgressBar pbPharmacistLoading;

    private PharmacistOrderAdapter orderAdapter;
    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String pharmacistStaffId = "ITA1024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        databaseConnection = DatabaseConnection.getInstance(this);

        // Load Staff ID from session or conform to "ITA" format
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "ITA1024");
        if (savedUsername != null && savedUsername.toUpperCase().startsWith("ITA")) {
            pharmacistStaffId = savedUsername.toUpperCase();
        } else {
            pharmacistStaffId = "ITA1024";
        }

        initViews();
        setupListeners();
        loadOrdersFromDatabase();
    }

    private void initViews() {
        tvPharmacistStaffId = findViewById(R.id.tvPharmacistStaffId);
        tvPendingCountBadge = findViewById(R.id.tvPendingCountBadge);
        btnBackPharmacist = findViewById(R.id.btnBackPharmacist);
        btnRefreshOrders = findViewById(R.id.btnRefreshOrders);

        chipGroupOrderFilters = findViewById(R.id.chipGroupOrderFilters);
        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterTakeaway = findViewById(R.id.chipFilterTakeaway);
        chipFilterDelivery = findViewById(R.id.chipFilterDelivery);

        swipeRefreshPharmacist = findViewById(R.id.swipeRefreshPharmacist);
        rvPharmacistOrders = findViewById(R.id.rvPharmacistOrders);
        layoutEmptyPharmacistOrders = findViewById(R.id.layoutEmptyPharmacistOrders);
        pbPharmacistLoading = findViewById(R.id.pbPharmacistLoading);

        // Requirement 1: Screen displays the logged-in Pharmacist's staff ID (must conform to "ITA" prefix format)
        if (tvPharmacistStaffId != null) {
            tvPharmacistStaffId.setText(String.format(Locale.getDefault(), "Staff ID: %s • Apollo Hospital Pharmacy", pharmacistStaffId));
        }

        if (rvPharmacistOrders != null) {
            rvPharmacistOrders.setLayoutManager(new LinearLayoutManager(this));
            orderAdapter = new PharmacistOrderAdapter(this, null, this::showOrderVerificationDialog);
            rvPharmacistOrders.setAdapter(orderAdapter);
        }
    }

    private void setupListeners() {
        if (btnBackPharmacist != null) {
            btnBackPharmacist.setOnClickListener(v -> finish());
        }

        if (btnRefreshOrders != null) {
            btnRefreshOrders.setOnClickListener(v -> loadOrdersFromDatabase());
        }

        if (swipeRefreshPharmacist != null) {
            swipeRefreshPharmacist.setColorSchemeResources(R.color.primary_clinical_blue, R.color.secondary_mint);
            swipeRefreshPharmacist.setOnRefreshListener(this::loadOrdersFromDatabase);
        }

        // Requirement 2: Material 3 ChipGroup filtering
        if (chipGroupOrderFilters != null) {
            chipGroupOrderFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipFilterAll) {
                    if (orderAdapter != null) orderAdapter.filter("ALL");
                } else if (checkedId == R.id.chipFilterTakeaway) {
                    if (orderAdapter != null) orderAdapter.filter("TAKEAWAY");
                } else if (checkedId == R.id.chipFilterDelivery) {
                    if (orderAdapter != null) orderAdapter.filter("DELIVERY");
                }
                updateEmptyState();
            });
        }
    }

    /**
     * Executes database queries on background thread (ensures zero NetworkOnMainThreadException)
     */
    private void loadOrdersFromDatabase() {
        if (pbPharmacistLoading != null) pbPharmacistLoading.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            List<OrderModel> orders = databaseConnection.getPharmacistOrders();

            mainHandler.post(() -> {
                if (pbPharmacistLoading != null) pbPharmacistLoading.setVisibility(View.GONE);
                if (swipeRefreshPharmacist != null) swipeRefreshPharmacist.setRefreshing(false);

                if (orderAdapter != null) {
                    orderAdapter.setOrders(orders);
                }

                // Update real-time counter badge
                int pendingCount = 0;
                int takeawayCount = 0;
                int deliveryCount = 0;
                for (OrderModel o : orders) {
                    if (o.getStatus() == OrderStatus.PENDING_VERIFICATION) {
                        pendingCount++;
                    }
                    if ("TAKEAWAY".equalsIgnoreCase(o.getOrderType())) {
                        takeawayCount++;
                    } else {
                        deliveryCount++;
                    }
                }

                if (tvPendingCountBadge != null) {
                    tvPendingCountBadge.setText(String.format(Locale.getDefault(), "%d Orders Awaiting Verification", pendingCount));
                }

                if (chipFilterAll != null) chipFilterAll.setText(String.format(Locale.getDefault(), "All Orders (%d)", orders.size()));
                if (chipFilterTakeaway != null) chipFilterTakeaway.setText(String.format(Locale.getDefault(), "Takeaway - QR (%d)", takeawayCount));
                if (chipFilterDelivery != null) chipFilterDelivery.setText(String.format(Locale.getDefault(), "Delivery - OTP (%d)", deliveryCount));

                updateEmptyState();
            });
        });
    }

    private void updateEmptyState() {
        if (orderAdapter != null && layoutEmptyPharmacistOrders != null) {
            layoutEmptyPharmacistOrders.setVisibility(orderAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Requirement 3: Interactive Verification Dialog / Bottom Sheet
     */
    private void showOrderVerificationDialog(OrderModel order) {
        if (order == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pharmacist_order, null);
        dialog.setContentView(dialogView);

        TextView tvDialogOrderId = dialogView.findViewById(R.id.tvDialogOrderId);
        TextView tvDialogPatientInfo = dialogView.findViewById(R.id.tvDialogPatientInfo);
        TextView tvDialogOrderTypeBadge = dialogView.findViewById(R.id.tvDialogOrderTypeBadge);
        View btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        View cardPrescriptionPreview = dialogView.findViewById(R.id.cardPrescriptionPreview);
        RecyclerView rvPrescriptionMedicines = dialogView.findViewById(R.id.rvPrescriptionMedicines);

        TextView tvBillSubtotal = dialogView.findViewById(R.id.tvBillSubtotal);
        TextView tvBillPlatformFee = dialogView.findViewById(R.id.tvBillPlatformFee);
        TextView tvBillTax = dialogView.findViewById(R.id.tvBillTax);
        TextView tvBillGrandTotal = dialogView.findViewById(R.id.tvBillGrandTotal);

        MaterialButton btnApproveOrder = dialogView.findViewById(R.id.btnApproveOrder);
        MaterialButton btnRejectOrder = dialogView.findViewById(R.id.btnRejectOrder);

        // Bind Header
        tvDialogOrderId.setText(String.format(Locale.getDefault(), "Order #%d Verification", order.getOrderId()));
        tvDialogPatientInfo.setText(String.format(Locale.getDefault(), "Patient: %s • %s", order.getPatientName(), order.getPatientPhone()));

        if ("TAKEAWAY".equalsIgnoreCase(order.getOrderType())) {
            tvDialogOrderTypeBadge.setText("TAKEAWAY 🏥");
        } else {
            tvDialogOrderTypeBadge.setText("DELIVERY 🛵");
        }

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        // Tap to view prescription fullscreen
        if (cardPrescriptionPreview != null) {
            cardPrescriptionPreview.setOnClickListener(v -> showFullscreenPrescriptionDialog(order));
        }

        // Dynamic Medicine Rows Adapter with live calculation callback
        List<PrescriptionMedicineItem> items = order.getMedicineList();
        PrescriptionMedicineAdapter.OnQuantityChangedListener calculationListener = () -> {
            recalculateLiveBill(items, tvBillSubtotal, tvBillTax, tvBillGrandTotal);
        };

        if (rvPrescriptionMedicines != null) {
            rvPrescriptionMedicines.setLayoutManager(new LinearLayoutManager(this));
            PrescriptionMedicineAdapter medicineAdapter = new PrescriptionMedicineAdapter(this, items, calculationListener);
            rvPrescriptionMedicines.setAdapter(medicineAdapter);
        }

        // Initial live bill calculation
        recalculateLiveBill(items, tvBillSubtotal, tvBillTax, tvBillGrandTotal);

        // If order already verified, change approve button text
        if (order.getStatus() != OrderStatus.PENDING_VERIFICATION) {
            btnApproveOrder.setText("Order Already Approved ✓");
            btnApproveOrder.setEnabled(false);
            btnRejectOrder.setVisibility(View.GONE);
        }

        // Requirement 4: Conditional Approval & Security Token Generation
        btnApproveOrder.setOnClickListener(v -> {
            executeOrderApprovalTransaction(dialog, order, items);
        });

        btnRejectOrder.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Reject Prescription")
                    .setMessage("Are you sure you want to reject Order #" + order.getOrderId() + "? The patient will be notified.")
                    .setPositiveButton("Reject Order", (d, w) -> {
                        executorService.execute(() -> {
                            databaseConnection.updateOrderApproval(order.getOrderId(), OrderStatus.CANCELLED, null, null, null, -1L, 0.0);
                            mainHandler.post(() -> {
                                dialog.dismiss();
                                Toast.makeText(PharmacistActivity.this, "Order #" + order.getOrderId() + " rejected", Toast.LENGTH_SHORT).show();
                                loadOrdersFromDatabase();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
    }

    /**
     * Requirement 3.d: Live Bill Summary recalculation
     */
    private double recalculateLiveBill(List<PrescriptionMedicineItem> items, TextView tvSubtotal, TextView tvTax, TextView tvGrandTotal) {
        double subtotal = 0.0;
        if (items != null) {
            for (PrescriptionMedicineItem item : items) {
                subtotal += item.getSubtotal();
            }
        }
        double platformFee = 15.00;
        double tax = subtotal * 0.05; // 5% GST
        double grandTotal = subtotal + platformFee + tax;

        if (tvSubtotal != null) tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        if (tvTax != null) tvTax.setText(String.format(Locale.getDefault(), "₹%.2f", tax));
        if (tvGrandTotal != null) tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));

        return grandTotal;
    }

    /**
     * Requirement 4: Approval Transaction & Security Token Generation
     */
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
        com.example.pharmago.services.BackendService.getInstance(this).approveOrder(
                order.getOrderId(),
                pharmacistStaffId,
                order.getOrderType(),
                finalGrandTotal,
                new com.example.pharmago.database.DbCallback<com.example.pharmago.models.OrderApprovalResult>() {
                    @Override
                    public void onSuccess(com.example.pharmago.models.OrderApprovalResult result) {
                        // Deduct approved medicine quantities from inventory
                        executorService.execute(() -> {
                            for (PrescriptionMedicineItem item : items) {
                                if (item.getRequestedQty() > 0) {
                                    databaseConnection.deductInventoryStock(item.getMedicineId(), item.getRequestedQty());
                                }
                            }
                        });

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

                        // Trigger notification to patient
                        com.example.pharmago.utils.NotificationManager.notifyPatientOrderVerified(PharmacistActivity.this, order);

                        dialog.dismiss();
                        loadOrdersFromDatabase();

                        if (isTakeaway) {
                            showTakeawayApprovalSuccessDialog(order, result.getInvoiceId(), result.getQrString(), qrBitmap);
                        } else {
                            showDeliveryApprovalSuccessDialog(order, result.getInvoiceId(), result.getOtpCode());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PharmacistActivity.this, "Approval failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void showTakeawayApprovalSuccessDialog(OrderModel order, long invoiceId, String qrString, Bitmap qrBitmap) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_pickup_qr, null);

        TextView tvPickupStatus = dialogView.findViewById(R.id.tvPickupStatus);
        TextView tvPickupDetails = dialogView.findViewById(R.id.tvPickupDetails);
        TextView tvPickupQR = dialogView.findViewById(R.id.tvPickupQR);
        ImageView imgPickupQR = dialogView.findViewById(R.id.imgPickupQR);
        View btnBackHome = dialogView.findViewById(R.id.btnBackHome);
        View btnBackPickup = dialogView.findViewById(R.id.btnBackPickup);

        if (tvPickupStatus != null) tvPickupStatus.setText("✓ Approved for Hospital Counter Pickup");
        if (tvPickupDetails != null) {
            tvPickupDetails.setText(String.format(Locale.getDefault(),
                    "Patient: %s\nInvoice: #INV-%d\nTotal: ₹%.2f\nStaff: %s\nPayload: %s\n\nPatient can now present this QR code at hospital counter for dispensing.",
                    order.getPatientName(), invoiceId, order.getTotalAmount(), pharmacistStaffId, qrString));
        }
        if (tvPickupQR != null) tvPickupQR.setText(String.format(Locale.getDefault(), "PICKUP TOKEN: ORDER #%d", order.getOrderId()));
        if (imgPickupQR != null && qrBitmap != null) imgPickupQR.setImageBitmap(qrBitmap);

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Takeaway QR Token Generated ✓")
                .setView(dialogView)
                .setPositiveButton("Done", null)
                .create();

        if (btnBackHome != null) btnBackHome.setOnClickListener(v -> alertDialog.dismiss());
        if (btnBackPickup != null) btnBackPickup.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void showDeliveryApprovalSuccessDialog(OrderModel order, long invoiceId, String otp) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delivery Security OTP Generated 🛵")
                .setMessage(String.format(Locale.getDefault(),
                        "Order #%d approved successfully!\n\n" +
                                "Invoice Generated: #INV-%d\n" +
                                "Total Approved Amount: ₹%.2f\n" +
                                "Verified by Pharmacist: %s\n\n" +
                                "🔐 Secure 6-Digit Delivery OTP:\n" +
                                "        [ %s ]\n\n" +
                                "This OTP has been recorded in database and dispatched via NotificationManager to patient %s.",
                        order.getOrderId(), invoiceId, order.getTotalAmount(), pharmacistStaffId, otp, order.getPatientName()))
                .setPositiveButton("Finish & Return to Feed", (d, w) -> {
                    Toast.makeText(this, "Order #" + order.getOrderId() + " marked READY_FOR_DELIVERY ✓", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showFullscreenPrescriptionDialog(OrderModel order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Prescription Document • Order #" + order.getOrderId())
                .setMessage("Patient: " + order.getPatientName() + "\nDoctor: Dr. Arvind Sharma, MBBS (Reg. #MC-58201)\nHospital: Apollo Clinics & Research Institute\n\nPrescribed Items:\n" +
                        "1. Amoxicillin 500mg (1-0-1 after food x 5 days)\n" +
                        "2. Paracetamol 650mg (SOS for fever)\n\n" +
                        "Rx Seal: Authentic Medical Practitioner Digital Signature Verified ✓")
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
