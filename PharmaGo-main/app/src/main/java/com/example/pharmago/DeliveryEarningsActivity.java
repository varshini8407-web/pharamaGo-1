package com.example.pharmago;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pharmago.adapters.DeliveryTripAdapter;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.DeliveryEarningsModel;
import com.example.pharmago.models.DeliveryTripModel;
import com.example.pharmago.services.BackendService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Member 3: Delivery Earnings & Analytics Activity
 * Displays:
 * - Today's Total Earnings ($ / ₹)
 * - Total Completed Deliveries count
 * - Weekly Earnings summary and 7-day mini bar chart
 * - Trip History list (Order ID, Delivery Distance, Timestamp, Delivery Payout, and Tip)
 * Hooked to MySQL query:
 * SELECT COUNT(*) as total_trips, SUM(payout) as total_earnings FROM delivery_trips WHERE agent_id = ? AND DATE(completed_at) = CURDATE();
 */
public class DeliveryEarningsActivity extends AppCompatActivity {

    private ImageView btnEarningsBack;
    private ImageView btnRefreshEarnings;
    private TextView tvEarningsAgentSubtitle;
    private SwipeRefreshLayout swipeRefreshEarnings;

    private TextView tvTodayEarningsTotal;
    private TextView tvTodayTripsBadge;
    private TextView tvTodayBasePayout;
    private TextView tvTodayTips;
    private TextView tvTodayAverage;

    private TextView tvWeeklyEarningsTotal;
    private View barMon, barTue, barWed, barThu, barFri, barSat, barToday;

    private TextView tvTripCountSubtitle;
    private RecyclerView rvTripHistory;
    private LinearLayout layoutEmptyTrips;

    private DeliveryTripAdapter tripAdapter;
    private final List<DeliveryTripModel> tripList = new ArrayList<>();
    private long currentAgentId = 402L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_earnings);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadEarningsData(false);
    }

    private void initViews() {
        btnEarningsBack = findViewById(R.id.btnEarningsBack);
        btnRefreshEarnings = findViewById(R.id.btnRefreshEarnings);
        tvEarningsAgentSubtitle = findViewById(R.id.tvEarningsAgentSubtitle);
        swipeRefreshEarnings = findViewById(R.id.swipeRefreshEarnings);

        tvTodayEarningsTotal = findViewById(R.id.tvTodayEarningsTotal);
        tvTodayTripsBadge = findViewById(R.id.tvTodayTripsBadge);
        tvTodayBasePayout = findViewById(R.id.tvTodayBasePayout);
        tvTodayTips = findViewById(R.id.tvTodayTips);
        tvTodayAverage = findViewById(R.id.tvTodayAverage);

        tvWeeklyEarningsTotal = findViewById(R.id.tvWeeklyEarningsTotal);
        barMon = findViewById(R.id.barMon);
        barTue = findViewById(R.id.barTue);
        barWed = findViewById(R.id.barWed);
        barThu = findViewById(R.id.barThu);
        barFri = findViewById(R.id.barFri);
        barSat = findViewById(R.id.barSat);
        barToday = findViewById(R.id.barToday);

        tvTripCountSubtitle = findViewById(R.id.tvTripCountSubtitle);
        rvTripHistory = findViewById(R.id.rvTripHistory);
        layoutEmptyTrips = findViewById(R.id.layoutEmptyTrips);

        swipeRefreshEarnings.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary_clinical_blue));

        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String uName = prefs.getString("username", "DEL402");
        String fName = prefs.getString("fullName", "Rajesh Kumar");
        tvEarningsAgentSubtitle.setText(String.format("Agent %s • %s", fName, uName));
    }

    private void setupRecyclerView() {
        tripAdapter = new DeliveryTripAdapter(this, tripList);
        rvTripHistory.setLayoutManager(new LinearLayoutManager(this));
        rvTripHistory.setAdapter(tripAdapter);
    }

    private void setupListeners() {
        btnEarningsBack.setOnClickListener(v -> finish());
        btnRefreshEarnings.setOnClickListener(v -> loadEarningsData(true));
        swipeRefreshEarnings.setOnRefreshListener(() -> loadEarningsData(true));
    }

    private void loadEarningsData(boolean isRefresh) {
        if (!isRefresh) {
            swipeRefreshEarnings.setRefreshing(true);
        }

        BackendService.getInstance(this).getDeliveryEarnings(currentAgentId, new DbCallback<DeliveryEarningsModel>() {
            @Override
            public void onSuccess(DeliveryEarningsModel model) {
                swipeRefreshEarnings.setRefreshing(false);
                if (model == null) return;

                bindEarningsModel(model);
            }

            @Override
            public void onFailure(Exception e) {
                swipeRefreshEarnings.setRefreshing(false);
                Toast.makeText(DeliveryEarningsActivity.this, "Failed to load live metrics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindEarningsModel(DeliveryEarningsModel model) {
        double totalToday = model.getTotalEarningsToday();
        long tripsToday = model.getTotalTripsToday();

        tvTodayEarningsTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalToday));
        tvTodayTripsBadge.setText(String.format(Locale.getDefault(), "%d Completed Trips", tripsToday));

        // Calculate base and tip breakdown from trips
        double basePayout = 0.0;
        double tips = 0.0;
        for (DeliveryTripModel t : model.getTripHistory()) {
            basePayout += t.getPayout();
            tips += t.getTip();
        }
        if (basePayout == 0.0) {
            basePayout = totalToday * 0.75;
            tips = totalToday * 0.25;
        }

        tvTodayBasePayout.setText(String.format(Locale.getDefault(), "₹%.2f", basePayout));
        tvTodayTips.setText(String.format(Locale.getDefault(), "₹%.2f", tips));

        double avg = tripsToday > 0 ? (totalToday / tripsToday) : 0.0;
        tvTodayAverage.setText(String.format(Locale.getDefault(), "₹%.2f", avg));

        // Weekly metrics
        tvWeeklyEarningsTotal.setText(String.format(Locale.getDefault(), "₹%.2f", model.getWeeklyEarningsTotal()));

        // Bar heights
        double[] weekly = model.getWeeklyBreakdown();
        if (weekly != null && weekly.length >= 7) {
            double max = 1.0;
            for (double d : weekly) {
                if (d > max) max = d;
            }
            applyBarHeight(barMon, weekly[0], max);
            applyBarHeight(barTue, weekly[1], max);
            applyBarHeight(barWed, weekly[2], max);
            applyBarHeight(barThu, weekly[3], max);
            applyBarHeight(barFri, weekly[4], max);
            applyBarHeight(barSat, weekly[5], max);
            applyBarHeight(barToday, weekly[6], max);
        }

        // Trip history list
        List<DeliveryTripModel> history = model.getTripHistory();
        tripList.clear();
        if (history != null && !history.isEmpty()) {
            tripList.addAll(history);
            tripAdapter.notifyDataSetChanged();
            rvTripHistory.setVisibility(View.VISIBLE);
            layoutEmptyTrips.setVisibility(View.GONE);
            tvTripCountSubtitle.setText(String.format(Locale.getDefault(), "%d completed records", tripList.size()));
        } else {
            tripAdapter.notifyDataSetChanged();
            rvTripHistory.setVisibility(View.GONE);
            layoutEmptyTrips.setVisibility(View.VISIBLE);
            tvTripCountSubtitle.setText("0 records");
        }
    }

    private void applyBarHeight(View bar, double value, double max) {
        if (bar == null) return;
        int maxDpHeight = 85;
        int minDpHeight = 15;
        float density = getResources().getDisplayMetrics().density;
        int calculatedDp = (int) (minDpHeight + ((value / max) * (maxDpHeight - minDpHeight)));
        int heightPx = (int) (calculatedDp * density);

        ViewGroup.LayoutParams params = bar.getLayoutParams();
        if (params != null) {
            params.height = heightPx;
            bar.setLayoutParams(params);
        }
    }
}
