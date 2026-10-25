package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.services.BackendService;
import com.example.pharmago.utils.PharmacistNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PharmacistHomeActivity extends AppCompatActivity {

    private TextView tvStaffId, tvBranchName, tvStoreStatusText;
    private TextView tvMetricPendingCount, tvMetricTodayRevenue, tvMetricPickupCount, tvMetricLowStockCount;
    private SwitchCompat switchStoreAvailability;
    private ImageView btnPharmacistLogout, btnRefreshDashboard;
    private MaterialCardView cardMetricPending, cardMetricRevenue, cardMetricPickup, cardMetricLowStock;
    private View btnQuickGoToOrders, btnQuickGoToInventory;
    private ExtendedFloatingActionButton fabScanTakeawayQR;
    private BottomNavigationView pharmacistBottomNav;

    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String staffId = "ITA1024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist_home);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pharmacistBottomNav != null) {
            pharmacistBottomNav.setSelectedItemId(R.id.nav_pharm_dashboard);
        }
        loadDashboardMetrics();
    }

    private void initViews() {
        tvStaffId = findViewById(R.id.tvStaffId);
        tvBranchName = findViewById(R.id.tvBranchName);
        tvStoreStatusText = findViewById(R.id.tvStoreStatusText);

        tvMetricPendingCount = findViewById(R.id.tvMetricPendingCount);
        tvMetricTodayRevenue = findViewById(R.id.tvMetricTodayRevenue);
        tvMetricPickupCount = findViewById(R.id.tvMetricPickupCount);
        tvMetricLowStockCount = findViewById(R.id.tvMetricLowStockCount);

        switchStoreAvailability = findViewById(R.id.switchStoreAvailability);
        btnPharmacistLogout = findViewById(R.id.btnPharmacistLogout);
        btnRefreshDashboard = findViewById(R.id.btnRefreshDashboard);

        cardMetricPending = findViewById(R.id.cardMetricPending);
        cardMetricRevenue = findViewById(R.id.cardMetricRevenue);
        cardMetricPickup = findViewById(R.id.cardMetricPickup);
        cardMetricLowStock = findViewById(R.id.cardMetricLowStock);

        btnQuickGoToOrders = findViewById(R.id.btnQuickGoToOrders);
        btnQuickGoToInventory = findViewById(R.id.btnQuickGoToInventory);
        fabScanTakeawayQR = findViewById(R.id.fabScanTakeawayQR);
        pharmacistBottomNav = findViewById(R.id.pharmacistBottomNav);

        tvStaffId.setText("Staff ID: " + staffId);

        PharmacistNavigationHelper.setupBottomNavigation(this, pharmacistBottomNav, R.id.nav_pharm_dashboard);
    }

    private void setupListeners() {
        btnPharmacistLogout.setOnClickListener(v -> {
            Intent intent = new Intent(PharmacistHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnRefreshDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing clinical metrics...", Toast.LENGTH_SHORT).show();
            loadDashboardMetrics();
        });

        switchStoreAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStoreStatusText.setText("Dispensing Active • Online");
                tvStoreStatusText.setTextColor(0xFFA7F3D0);
                Toast.makeText(this, "Pharmacy Counter is OPEN and accepting orders", Toast.LENGTH_SHORT).show();
            } else {
                tvStoreStatusText.setText("Counter Paused • Offline");
                tvStoreStatusText.setTextColor(0xFFFECACA);
                Toast.makeText(this, "Pharmacy Counter is temporarily PAUSED", Toast.LENGTH_SHORT).show();
            }
        });

        cardMetricPending.setOnClickListener(v -> openOrdersActivity());
        btnQuickGoToOrders.setOnClickListener(v -> openOrdersActivity());

        cardMetricRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PharmacistReportsActivity.class);
            startActivity(intent);
        });

        cardMetricPickup.setOnClickListener(v -> openQRScanCounter());
        fabScanTakeawayQR.setOnClickListener(v -> openQRScanCounter());

        cardMetricLowStock.setOnClickListener(v -> openInventoryActivity());
        btnQuickGoToInventory.setOnClickListener(v -> openInventoryActivity());
    }

    private void openOrdersActivity() {
        Intent intent = new Intent(this, PharmacistOrdersActivity.class);
        startActivity(intent);
    }

    private void openQRScanCounter() {
        Intent intent = new Intent(this, QRScanCounterActivity.class);
        startActivity(intent);
    }

    private void openInventoryActivity() {
        Intent intent = new Intent(this, ClinicMedicineActivity.class);
        startActivity(intent);
    }

    private void loadDashboardMetrics() {
        BackendService.getInstance(this).getDashboardMetrics(new DbCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> metrics) {
                if (metrics == null) return;
                int pending = metrics.containsKey("pendingCount") ? (int) metrics.get("pendingCount") : 0;
                double revenue = metrics.containsKey("todayRevenue") ? (double) metrics.get("todayRevenue") : 0.0;
                int pickup = metrics.containsKey("pickupCount") ? (int) metrics.get("pickupCount") : 0;
                int lowStock = metrics.containsKey("lowStockCount") ? (int) metrics.get("lowStockCount") : 0;

                tvMetricPendingCount.setText(String.valueOf(pending));
                tvMetricTodayRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", revenue));
                tvMetricPickupCount.setText(String.valueOf(pickup));
                tvMetricLowStockCount.setText(String.valueOf(lowStock));
            }

            @Override
            public void onFailure(Exception e) {
                // Graceful fallback to local cache
                Map<String, Object> local = databaseConnection.getDashboardMetrics();
                if (local != null) {
                    tvMetricPendingCount.setText(String.valueOf(local.get("pendingCount")));
                    tvMetricTodayRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", (double) local.get("todayRevenue")));
                    tvMetricPickupCount.setText(String.valueOf(local.get("pickupCount")));
                    tvMetricLowStockCount.setText(String.valueOf(local.get("lowStockCount")));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
