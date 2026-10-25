package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.adapters.OrderHistoryAdapter;
import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.OrderModel;
import com.example.pharmago.models.OrderResponse;
import com.example.pharmago.services.BackendService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etSearchOrders;
    View tvEmptyHistory;
    ProgressBar progressBar;
    LinearLayout orderContainer;
    OrderHistoryAdapter orderAdapter;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requirement 2: setContentView strictly BEFORE any findViewById
        setContentView(R.layout.activity_order_history);

        // Cross-referenced IDs against activity_order_history.xml
        recyclerView = findViewById(R.id.recyclerView);
        etSearchOrders = findViewById(R.id.etSearchOrders);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        progressBar = findViewById(R.id.progressBar);
        orderContainer = findViewById(R.id.orderContainer);

        View btnBackOrders = findViewById(R.id.btnBackOrders);
        if (btnBackOrders != null) {
            btnBackOrders.setOnClickListener(v -> finish());
        }

        // Requirement 3: Ensure RecyclerView is initialized with a LinearLayoutManager
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }

        // Requirement 3: Inject 3-5 items of hardcoded mock data so the list renders cards immediately upon launch
        List<OrderResponse> mockOrders = createMockOrders();
        orderAdapter = new OrderHistoryAdapter(mockOrders, order -> {
            if (order == null) return;
            if ("DELIVERY".equalsIgnoreCase(order.getType())) {
                if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
                    SharedPreferences prefs = getSharedPreferences("PharmaGoOrders", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("hasActiveLiveOrder", true)
                            .putLong("activeOrderId", order.getId() != null ? order.getId() : 103L)
                            .putString("activePharmacyName", order.getPharmacyName())
                            .putString("activePrice", order.getTotalPrice() != null ? "₹" + String.format("%.2f", order.getTotalPrice()) : "₹90.00")
                            .putString("activeStatus", order.getStatus() != null ? order.getStatus() : "IN TRANSIT")
                            .putString("activeDeliveryAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Apt 402, Green Avenue, Sector 4, Metro City")
                            .putLong("activeOrderPlacedTime", System.currentTimeMillis())
                            .apply();
                }

                Intent intent = new Intent(OrderHistoryActivity.this, DeliveryTrackingActivity.class);
                intent.putExtra("orderId", order.getId());
                intent.putExtra("pharmacyName", order.getPharmacyName());
                intent.putExtra("price", order.getTotalPrice() != null ? "₹" + String.format("%.2f", order.getTotalPrice()) : "₹90.00");
                intent.putExtra("status", order.getStatus());
                intent.putExtra("deliveryAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Apt 402, Green Avenue, Sector 4, Metro City");
                startActivity(intent);
            } else {
                Intent intent = new Intent(OrderHistoryActivity.this, PickupQRActivity.class);
                intent.putExtra("orderId", order.getId());
                intent.putExtra("pharmacyName", order.getPharmacyName());
                intent.putExtra("price", order.getTotalPrice() != null ? "₹" + String.format("%.2f", order.getTotalPrice()) : "₹120.00");
                intent.putExtra("status", order.getStatus());
                startActivity(intent);
            }
        });

        if (recyclerView != null) {
            recyclerView.setAdapter(orderAdapter);
        }

        // Requirement 3: TextWatcher listener that filters this mock list locally
        if (etSearchOrders != null) {
            etSearchOrders.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (orderAdapter != null) {
                        orderAdapter.filter(s != null ? s.toString() : "");
                        if (tvEmptyHistory != null) {
                            tvEmptyHistory.setVisibility(orderAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Attempt server fetch safely without breaking mock data if offline
        loadOrderHistoryFromServerSafely();
    }

    private List<OrderResponse> createMockOrders() {
        List<OrderResponse> list = new ArrayList<>();

        OrderResponse o1 = new OrderResponse();
        o1.setId(101L);
        o1.setPharmacyName("Apollo Pharmacy (Paracetamol 500mg)");
        o1.setType("DELIVERY");
        o1.setStatus("DELIVERED");
        o1.setTotalPrice(90.00);
        o1.setOrderDate("2026-10-20");
        list.add(o1);

        OrderResponse o2 = new OrderResponse();
        o2.setId(102L);
        o2.setPharmacyName("MedPlus Wellness (Amoxicillin 250mg)");
        o2.setType("TAKEAWAY");
        o2.setStatus("READY FOR PICKUP");
        o2.setTotalPrice(120.00);
        o2.setOrderDate("2026-10-22");
        list.add(o2);

        OrderResponse o3 = new OrderResponse();
        o3.setId(103L);
        o3.setPharmacyName("CarePoint Chemist (Cetirizine 10mg)");
        o3.setType("DELIVERY");
        o3.setStatus("IN TRANSIT");
        o3.setTotalPrice(35.00);
        o3.setOrderDate("2026-10-24");
        list.add(o3);

        OrderResponse o4 = new OrderResponse();
        o4.setId(104L);
        o4.setPharmacyName("City Meds (Ibuprofen 400mg)");
        o4.setType("DELIVERY");
        o4.setStatus("DELIVERED");
        o4.setTotalPrice(60.00);
        o4.setOrderDate("2026-10-25");
        list.add(o4);

        return list;
    }

    private void loadOrderHistoryFromServerSafely() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        attemptRetrofitFetch();
    }

    private void attemptRetrofitFetch() {
        try {
            apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);
            apiService.getMyOrders().enqueue(new Callback<List<OrderResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (orderAdapter != null) {
                            orderAdapter.updateData(response.body());
                        }
                    } else {
                        fallbackBackendServiceOrders();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                    fallbackBackendServiceOrders();
                }
            });
        } catch (Exception e) {
            fallbackBackendServiceOrders();
        }
    }

    private void fallbackBackendServiceOrders() {
        SharedPreferences userPrefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        Long patientId = userPrefs.getLong("userId", 101L);

        BackendService.getInstance(this).getPatientOrders(patientId, new DbCallback<List<OrderModel>>() {
            @Override
            public void onSuccess(List<OrderModel> orderModels) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (orderModels != null && !orderModels.isEmpty()) {
                    List<OrderResponse> responseList = new ArrayList<>();
                    for (OrderModel m : orderModels) {
                        OrderResponse resp = new OrderResponse();
                        resp.setId(m.getOrderId());
                        resp.setPharmacyName(m.getPatientName() != null ? "Prescription Order • " + m.getOrderType() : "Hospital Central Pharmacy");
                        resp.setType(m.getOrderType());
                        resp.setStatus(m.getOrderStatus() != null ? m.getOrderStatus().name() : "PENDING");
                        resp.setTotalPrice(m.getTotalAmount());
                        resp.setOrderDate(m.getTimestamp() != null ? m.getTimestamp() : "Today");
                        resp.setDeliveryAddress(m.getDeliveryAddress());
                        responseList.add(resp);
                    }
                    if (orderAdapter != null) {
                        orderAdapter.updateData(responseList);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }
}