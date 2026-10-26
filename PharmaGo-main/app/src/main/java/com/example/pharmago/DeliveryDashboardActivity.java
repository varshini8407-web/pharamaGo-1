package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pharmago.adapters.DeliveryOrderAdapter;
import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.services.DeliveryService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Member 3: Delivery Dashboard Activity (DeliveryDashboardActivity)
 * Central hub for hospital and pharmacy delivery agents:
 * - Active shift status toggle (Online/Offline)
 * - 2-Card Metric Summary (Assigned Deliveries & Completed Today)
 * - 3-Tab Feed (New Assignments, In Transit / Active, Completed History)
 * - Turn-by-turn routing navigation, quick phone calling, and 6-digit OTP verification.
 */
public class DeliveryDashboardActivity extends AppCompatActivity implements DeliveryOrderAdapter.OnDeliveryOrderClickListener {

    private ImageView btnDeliveryLogout;
    private TextView tvDeliveryAgentName;
    private ImageView btnRefreshDeliveryDashboard;

    private ImageView ivShiftStatusIcon;
    private TextView tvShiftStatusTitle;
    private TextView tvShiftStatusSub;
    private SwitchCompat switchAgentShift;

    private TextView tvMetricAssignedCount;
    private TextView tvMetricCompletedToday;

    private TabLayout tabLayoutDelivery;
    private SwipeRefreshLayout swipeRefreshDelivery;
    private RecyclerView rvDeliveryOrders;
    private LinearLayout layoutEmptyDeliveryState;
    private ProgressBar pbDeliveryLoading;

    private DeliveryOrderAdapter orderAdapter;
    private List<DeliveryOrderModel> allOrdersList = new ArrayList<>();
    private String currentSelectedTab = "NEW";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private DeliveryService deliveryService;

    // Activity Result Launcher for DeliveryMapActivity
    private final ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Order was verified and marked delivered in Map screen!
                    Toast.makeText(this, "Delivery completed successfully!", Toast.LENGTH_SHORT).show();
                    loadOrdersAndMetrics(true);
                    // Automatically switch to Completed History tab
                    TabLayout.Tab historyTab = tabLayoutDelivery.getTabAt(2);
                    if (historyTab != null) {
                        historyTab.select();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);

        deliveryService = DeliveryService.getInstance(this);

        initViews();
        setupRecyclerView();
        setupTabs();
        setupListeners();
        loadOrdersAndMetrics(false);
    }

    private void initViews() {
        btnDeliveryLogout = findViewById(R.id.btnDeliveryLogout);
        tvDeliveryAgentName = findViewById(R.id.tvDeliveryAgentName);
        btnRefreshDeliveryDashboard = findViewById(R.id.btnRefreshDeliveryDashboard);

        ivShiftStatusIcon = findViewById(R.id.ivShiftStatusIcon);
        tvShiftStatusTitle = findViewById(R.id.tvShiftStatusTitle);
        tvShiftStatusSub = findViewById(R.id.tvShiftStatusSub);
        switchAgentShift = findViewById(R.id.switchAgentShift);

        tvMetricAssignedCount = findViewById(R.id.tvMetricAssignedCount);
        tvMetricCompletedToday = findViewById(R.id.tvMetricCompletedToday);

        tabLayoutDelivery = findViewById(R.id.tabLayoutDelivery);
        swipeRefreshDelivery = findViewById(R.id.swipeRefreshDelivery);
        rvDeliveryOrders = findViewById(R.id.rvDeliveryOrders);
        layoutEmptyDeliveryState = findViewById(R.id.layoutEmptyDeliveryState);
        pbDeliveryLoading = findViewById(R.id.pbDeliveryLoading);

        swipeRefreshDelivery.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary_clinical_blue));
    }

    private void setupRecyclerView() {
        orderAdapter = new DeliveryOrderAdapter(this, allOrdersList, this);
        rvDeliveryOrders.setLayoutManager(new LinearLayoutManager(this));
        rvDeliveryOrders.setAdapter(orderAdapter);
    }

    private void setupTabs() {
        tabLayoutDelivery.clearOnTabSelectedListeners();
        tabLayoutDelivery.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentSelectedTab = "NEW";
                } else if (position == 1) {
                    currentSelectedTab = "ACTIVE";
                } else {
                    currentSelectedTab = "HISTORY";
                }
                applyFilterAndRefreshUI();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupListeners() {
        // Logout action
        btnDeliveryLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DeliveryDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Top refresh button
        btnRefreshDeliveryDashboard.setOnClickListener(v -> loadOrdersAndMetrics(true));

        // Earnings button navigation
        View btnDeliveryEarningsNav = findViewById(R.id.btnDeliveryEarningsNav);
        if (btnDeliveryEarningsNav != null) {
            btnDeliveryEarningsNav.setOnClickListener(v -> startActivity(new Intent(this, DeliveryEarningsActivity.class)));
        }

        // Profile button navigation
        View btnDeliveryProfileNav = findViewById(R.id.btnDeliveryProfileNav);
        if (btnDeliveryProfileNav != null) {
            btnDeliveryProfileNav.setOnClickListener(v -> startActivity(new Intent(this, DeliveryProfileActivity.class)));
        }
        if (tvDeliveryAgentName != null) {
            SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
            String uName = prefs.getString("username", "DEL402");
            String fName = prefs.getString("fullName", "Rajesh Kumar");
            tvDeliveryAgentName.setText(fName + " • " + uName);
            tvDeliveryAgentName.setOnClickListener(v -> startActivity(new Intent(this, DeliveryProfileActivity.class)));
        }

        // SwipeRefresh pull-to-refresh
        swipeRefreshDelivery.setOnRefreshListener(() -> loadOrdersAndMetrics(true));

        // Active shift toggle
        switchAgentShift.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ivShiftStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_mint)));
                tvShiftStatusTitle.setText("On Duty • Receiving Orders");
                tvShiftStatusSub.setText("Ready for hospital prescription deliveries");
                Toast.makeText(this, "Shift status: ONLINE", Toast.LENGTH_SHORT).show();
            } else {
                ivShiftStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.neutral_muted)));
                tvShiftStatusTitle.setText("Off Duty • Shift Paused");
                tvShiftStatusSub.setText("Paused delivery requests for break / rest");
                Toast.makeText(this, "Shift status: OFFLINE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrdersAndMetrics(boolean isRefresh) {
        if (!isRefresh) {
            pbDeliveryLoading.setVisibility(View.VISIBLE);
        }

        com.example.pharmago.services.BackendService.getInstance(this).getAssignedDeliveries(
                402L,
                new com.example.pharmago.database.DbCallback<List<DeliveryOrderModel>>() {
                    @Override
                    public void onSuccess(List<DeliveryOrderModel> fetched) {
                        pbDeliveryLoading.setVisibility(View.GONE);
                        swipeRefreshDelivery.setRefreshing(false);

                        allOrdersList.clear();
                        if (fetched != null && !fetched.isEmpty()) {
                            allOrdersList.addAll(fetched);
                        } else {
                            allOrdersList.addAll(deliveryService.getDeliveryOrders(null));
                        }

                        int assigned = 0;
                        int completed = 0;
                        for (DeliveryOrderModel o : allOrdersList) {
                            if (o.getOrderStatus() == OrderStatus.READY_FOR_DELIVERY || o.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY) {
                                assigned++;
                            } else if (o.getOrderStatus() == OrderStatus.DELIVERED || o.getOrderStatus() == OrderStatus.COMPLETED) {
                                completed++;
                            }
                        }

                        tvMetricAssignedCount.setText(String.valueOf(assigned));
                        tvMetricCompletedToday.setText(String.valueOf(completed));

                        updateTabCounters();
                        applyFilterAndRefreshUI();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        executorService.execute(() -> {
                            List<DeliveryOrderModel> fetched = deliveryService.getDeliveryOrders(null);
                            Map<String, Integer> metrics = deliveryService.getDeliveryMetrics();

                            mainHandler.post(() -> {
                                pbDeliveryLoading.setVisibility(View.GONE);
                                swipeRefreshDelivery.setRefreshing(false);

                                allOrdersList.clear();
                                if (fetched != null) {
                                    allOrdersList.addAll(fetched);
                                }

                                int assigned = metrics != null && metrics.containsKey("assignedCount") ? metrics.get("assignedCount") : 0;
                                int completed = metrics != null && metrics.containsKey("completedToday") ? metrics.get("completedToday") : 0;
                                tvMetricAssignedCount.setText(String.valueOf(assigned));
                                tvMetricCompletedToday.setText(String.valueOf(completed));

                                updateTabCounters();
                                applyFilterAndRefreshUI();
                            });
                        });
                    }
                }
        );
    }

    private void updateTabCounters() {
        int newCount = 0;
        int activeCount = 0;
        int historyCount = 0;

        for (DeliveryOrderModel order : allOrdersList) {
            OrderStatus status = order.getOrderStatus();
            if (status == OrderStatus.READY_FOR_DELIVERY) {
                newCount++;
            } else if (status == OrderStatus.OUT_FOR_DELIVERY) {
                activeCount++;
            } else if (status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED) {
                historyCount++;
            }
        }

        TabLayout.Tab tab0 = tabLayoutDelivery.getTabAt(0);
        if (tab0 != null) tab0.setText(String.format(Locale.getDefault(), "New (%d)", newCount));

        TabLayout.Tab tab1 = tabLayoutDelivery.getTabAt(1);
        if (tab1 != null) tab1.setText(String.format(Locale.getDefault(), "In Transit (%d)", activeCount));

        TabLayout.Tab tab2 = tabLayoutDelivery.getTabAt(2);
        if (tab2 != null) tab2.setText(String.format(Locale.getDefault(), "History (%d)", historyCount));
    }

    private void applyFilterAndRefreshUI() {
        orderAdapter.updateData(allOrdersList);
        orderAdapter.filterByTab(currentSelectedTab);

        if (orderAdapter.getItemCount() == 0) {
            layoutEmptyDeliveryState.setVisibility(View.VISIBLE);
            rvDeliveryOrders.setVisibility(View.GONE);
        } else {
            layoutEmptyDeliveryState.setVisibility(View.GONE);
            rvDeliveryOrders.setVisibility(View.VISIBLE);
        }
    }

    // --- OnDeliveryOrderClickListener Implementations ---

    @Override
    public void onAcceptOrder(DeliveryOrderModel order) {
        if (order == null) return;

        com.example.pharmago.services.BackendService.getInstance(this).startDelivery(
                order.getOrderId(),
                402L,
                new com.example.pharmago.database.DbCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        Toast.makeText(DeliveryDashboardActivity.this, "Order #" + order.getOrderId() + " accepted! Switched to In Transit via MySQL.", Toast.LENGTH_SHORT).show();
                        loadOrdersAndMetrics(true);

                        // Switch to In Transit tab
                        TabLayout.Tab activeTab = tabLayoutDelivery.getTabAt(1);
                        if (activeTab != null) {
                            activeTab.select();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(DeliveryDashboardActivity.this, "Failed to accept order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onDeclineOrder(DeliveryOrderModel order) {
        if (order == null) return;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Decline Delivery")
                .setMessage("Decline Order #" + order.getOrderId() + "? This order will remain in the pool for other delivery agents and will be removed from your queue.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Decline Order", (dialog, which) -> {
                    com.example.pharmago.services.BackendService.getInstance(this).declineDelivery(
                            order.getOrderId(),
                            402L,
                            "Declined by agent",
                            new com.example.pharmago.database.DbCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean success) {
                                    Toast.makeText(DeliveryDashboardActivity.this, "Order #" + order.getOrderId() + " declined and removed from feed.", Toast.LENGTH_SHORT).show();
                                    loadOrdersAndMetrics(true);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(DeliveryDashboardActivity.this, "Failed to decline order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                })
                .show();
    }

    @Override
    public void onOpenMapNavigation(DeliveryOrderModel order) {
        if (order == null) return;

        Intent intent = new Intent(this, DeliveryMapActivity.class);
        intent.putExtra(DeliveryMapActivity.EXTRA_DELIVERY_ORDER, order);
        mapActivityLauncher.launch(intent);
    }

    @Override
    public void onVerifyOtp(DeliveryOrderModel order) {
        if (order == null) return;

        OTPVerificationBottomSheet bottomSheet = OTPVerificationBottomSheet.newInstance(order);
        bottomSheet.setOnOtpVerificationListener(verifiedOrder -> {
            loadOrdersAndMetrics(true);

            // Switch to History tab
            TabLayout.Tab historyTab = tabLayoutDelivery.getTabAt(2);
            if (historyTab != null) {
                historyTab.select();
            }
        });
        bottomSheet.show(getSupportFragmentManager(), "OTPVerificationBottomSheet");
    }

    @Override
    public void onCallCustomer(DeliveryOrderModel order) {
        if (order == null || order.getPatientPhone() == null) {
            Toast.makeText(this, "Customer phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + order.getPatientPhone().trim()));
            startActivity(dialIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to initiate call", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrdersAndMetrics(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
