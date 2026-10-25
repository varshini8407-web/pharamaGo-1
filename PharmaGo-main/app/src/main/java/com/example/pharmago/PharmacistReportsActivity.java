package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.adapters.TopDispensedAdapter;
import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.services.BackendService;
import com.example.pharmago.utils.PharmacistNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PharmacistReportsActivity extends AppCompatActivity {

    private ImageView btnBackReports, btnExportReports;
    private ChipGroup chipGroupDateFilters;
    private TextView tvReportPeriodBadge, tvReportTotalRevenue, tvReportTotalPatients;
    private TextView tvReportTakeawayCount, tvReportDeliveryCount, tvReportUpiCount, tvReportCashCount;
    private RecyclerView rvTopDispensedMedicines;
    private BottomNavigationView pharmacistBottomNav;

    private TopDispensedAdapter topAdapter;
    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String activeDateFilter = "TODAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist_reports);

        databaseConnection = DatabaseConnection.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        loadReportsData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pharmacistBottomNav != null) {
            pharmacistBottomNav.setSelectedItemId(R.id.nav_pharm_reports);
        }
        loadReportsData();
    }

    private void initViews() {
        btnBackReports = findViewById(R.id.btnBackReports);
        btnExportReports = findViewById(R.id.btnExportReports);
        chipGroupDateFilters = findViewById(R.id.chipGroupDateFilters);

        tvReportPeriodBadge = findViewById(R.id.tvReportPeriodBadge);
        tvReportTotalRevenue = findViewById(R.id.tvReportTotalRevenue);
        tvReportTotalPatients = findViewById(R.id.tvReportTotalPatients);
        tvReportTakeawayCount = findViewById(R.id.tvReportTakeawayCount);
        tvReportDeliveryCount = findViewById(R.id.tvReportDeliveryCount);
        tvReportUpiCount = findViewById(R.id.tvReportUpiCount);
        tvReportCashCount = findViewById(R.id.tvReportCashCount);

        rvTopDispensedMedicines = findViewById(R.id.rvTopDispensedMedicines);
        pharmacistBottomNav = findViewById(R.id.pharmacistBottomNav);

        rvTopDispensedMedicines.setLayoutManager(new LinearLayoutManager(this));
        topAdapter = new TopDispensedAdapter(this, new ArrayList<>());
        rvTopDispensedMedicines.setAdapter(topAdapter);

        PharmacistNavigationHelper.setupBottomNavigation(this, pharmacistBottomNav, R.id.nav_pharm_reports);
    }

    private void setupListeners() {
        btnBackReports.setOnClickListener(v -> finish());

        btnExportReports.setOnClickListener(v -> {
            String reportText = "Hospital Pharmacy Report (" + activeDateFilter + ")\n" +
                    "• Total Revenue: " + tvReportTotalRevenue.getText() + "\n" +
                    "• Total Patient Orders: " + tvReportTotalPatients.getText() + "\n" +
                    "• Takeaway vs Delivery: " + tvReportTakeawayCount.getText() + " / " + tvReportDeliveryCount.getText() + "\n" +
                    "• Online UPI vs Cash: " + tvReportUpiCount.getText() + " / " + tvReportCashCount.getText();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hospital Pharmacy Clinical Report");
            shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
            startActivity(Intent.createChooser(shareIntent, "Share Clinical Financial Report"));
        });

        chipGroupDateFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipDateThisWeek)) {
                activeDateFilter = "THIS WEEK";
                tvReportPeriodBadge.setText("THIS WEEK");
            } else if (checkedIds.contains(R.id.chipDateThisMonth)) {
                activeDateFilter = "THIS MONTH";
                tvReportPeriodBadge.setText("THIS MONTH");
            } else {
                activeDateFilter = "TODAY";
                tvReportPeriodBadge.setText("TODAY");
            }
            loadReportsData();
        });
    }

    private void loadReportsData() {
        BackendService.getInstance(this).getPharmacyReports(activeDateFilter, new DbCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (data == null) return;
                double totalRevenue = data.containsKey("totalRevenue") ? (double) data.get("totalRevenue") : 0.0;
                int totalPatients = data.containsKey("totalPatients") ? (int) data.get("totalPatients") : 0;
                int takeawayCount = data.containsKey("takeawayCount") ? (int) data.get("takeawayCount") : 0;
                int deliveryCount = data.containsKey("deliveryCount") ? (int) data.get("deliveryCount") : 0;
                int cashCount = data.containsKey("cashCount") ? (int) data.get("cashCount") : 0;
                int upiCount = data.containsKey("upiCount") ? (int) data.get("upiCount") : 0;
                @SuppressWarnings("unchecked")
                List<InventoryItem> topMedicines = (List<InventoryItem>) data.get("topMedicines");

                tvReportTotalRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", totalRevenue));
                tvReportTotalPatients.setText("Across " + totalPatients + " verified hospital orders (" + activeDateFilter.toLowerCase() + ")");
                tvReportTakeawayCount.setText(String.valueOf(takeawayCount));
                tvReportDeliveryCount.setText(String.valueOf(deliveryCount));
                tvReportUpiCount.setText(String.valueOf(upiCount));
                tvReportCashCount.setText(String.valueOf(cashCount));

                if (topMedicines != null) {
                    topAdapter.updateData(topMedicines);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Fallback to local cache
                Map<String, Object> data = databaseConnection.getReportsData(activeDateFilter);
                if (data != null) {
                    tvReportTotalRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", (double) data.get("totalRevenue")));
                    tvReportTotalPatients.setText("Across " + data.get("totalPatients") + " verified hospital orders (" + activeDateFilter.toLowerCase() + ")");
                    tvReportTakeawayCount.setText(String.valueOf(data.get("takeawayCount")));
                    tvReportDeliveryCount.setText(String.valueOf(data.get("deliveryCount")));
                    tvReportUpiCount.setText(String.valueOf(data.get("upiCount")));
                    tvReportCashCount.setText(String.valueOf(data.get("cashCount")));
                    @SuppressWarnings("unchecked")
                    List<InventoryItem> topMedicines = (List<InventoryItem>) data.get("topMedicines");
                    if (topMedicines != null) {
                        topAdapter.updateData(topMedicines);
                    }
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
