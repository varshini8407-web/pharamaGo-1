package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class HomeActivity extends AppCompatActivity {

    View btnPharmacy, btnMedicine;
    Button btnPrescription, btnDelivery, btnTakeaway;
    View navOrders;
    TextView btnNotification, btnProfile, tvWelcome, tvHomeAddress;
    View layoutHomeLocation;
    EditText etHomeSearch;
    BottomNavigationView bottomNavigation;
    MaterialButtonToggleGroup toggleOrderType;

    View cardLiveTrackingHome;
    TextView tvHomeTrackingEta, tvHomeTrackingStatus;
    android.widget.ProgressBar pbHomeTracking;
    android.widget.ImageView btnDismissHomeTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requirement 2: setContentView strictly BEFORE any findViewById
        setContentView(R.layout.activity_home);

        // Bind all view elements
        btnPharmacy = findViewById(R.id.btnPharmacy);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnPrescription = findViewById(R.id.btnPrescription);
        btnDelivery = findViewById(R.id.btnDelivery);
        btnTakeaway = findViewById(R.id.btnTakeaway);
        navOrders = findViewById(R.id.navOrders);

        btnNotification = findViewById(R.id.btnNotification);
        btnProfile = findViewById(R.id.btnProfile);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvHomeAddress = findViewById(R.id.tvHomeAddress);
        layoutHomeLocation = findViewById(R.id.layoutHomeLocation);
        etHomeSearch = findViewById(R.id.etHomeSearch);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toggleOrderType = findViewById(R.id.toggleOrderType);

        cardLiveTrackingHome = findViewById(R.id.cardLiveTrackingHome);
        tvHomeTrackingEta = findViewById(R.id.tvHomeTrackingEta);
        pbHomeTracking = findViewById(R.id.pbHomeTracking);
        tvHomeTrackingStatus = findViewById(R.id.tvHomeTrackingStatus);
        btnDismissHomeTracking = findViewById(R.id.btnDismissHomeTracking);

        updateUserHeader();
        updateLiveTrackingCard();

        if (layoutHomeLocation != null) {
            layoutHomeLocation.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            });
        }

        // Setup M3 BottomNavigationView
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_search) {
                    startActivity(new Intent(HomeActivity.this, SearchActivity.class));
                    return true;
                } else if (id == R.id.nav_prescription) {
                    startActivity(new Intent(HomeActivity.this, PrescriptionUploadActivity.class));
                    return true;
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(HomeActivity.this, OrderHistoryActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            });
        }

        // Setup Order Type Toggle
        if (toggleOrderType != null) {
            toggleOrderType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnDelivery) {
                        Toast.makeText(HomeActivity.this, "Delivery mode selected", Toast.LENGTH_SHORT).show();
                    } else if (checkedId == R.id.btnTakeaway) {
                        Toast.makeText(HomeActivity.this, "Hospital pickup mode selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Quick Healthcare Actions
        if (btnPharmacy != null) {
            btnPharmacy.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        }

        if (btnMedicine != null) {
            btnMedicine.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MedicineSearchActivity.class)));
        }

        if (btnPrescription != null) {
            btnPrescription.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, PrescriptionUploadActivity.class)));
        }

        if (navOrders != null) {
            navOrders.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, OrderHistoryActivity.class)));
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));
        }

        View cardPharmacistPortalHome = findViewById(R.id.cardPharmacistPortalHome);
        if (cardPharmacistPortalHome != null) {
            cardPharmacistPortalHome.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, PharmacistHomeActivity.class)));
        }

        // Search Input handling
        if (etHomeSearch != null) {
            etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    String query = etHomeSearch.getText() != null ? etHomeSearch.getText().toString().trim() : "";
                    if (!query.isEmpty()) {
                        Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                        intent.putExtra("searchQuery", query);
                        startActivity(intent);
                        return true;
                    } else {
                        etHomeSearch.setError("Please enter a medicine or hospital name");
                    }
                }
                return false;
            });
        }
    }

    private void updateUserHeader() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String username = prefs != null ? prefs.getString("username", "Admin") : "Admin";
        String address = prefs != null ? prefs.getString("userAddress", "Home • Sector 4, Metro City") : "Home • Sector 4, Metro City";
        if (address == null || address.trim().isEmpty()) {
            address = "Home • Sector 4, Metro City";
        }
        if (tvWelcome != null) {
            tvWelcome.setText(getString(R.string.hello_user, username != null ? username : "Admin"));
        }
        if (tvHomeAddress != null) {
            tvHomeAddress.setText(address);
        }
    }

    private void updateLiveTrackingCard() {
        if (cardLiveTrackingHome == null) return;

        SharedPreferences prefs = getSharedPreferences("PharmaGoOrders", MODE_PRIVATE);
        boolean hasActiveOrder = prefs.getBoolean("hasActiveLiveOrder", false);

        if (!hasActiveOrder) {
            cardLiveTrackingHome.setVisibility(View.GONE);
            return;
        }

        long placedTime = prefs.getLong("activeOrderPlacedTime", 0L);
        long now = System.currentTimeMillis();
        long elapsedMinutes = placedTime > 0 ? (now - placedTime) / (60 * 1000) : 0;

        // If placed more than 30 minutes ago, auto-dismiss
        if (elapsedMinutes >= 30) {
            prefs.edit().putBoolean("hasActiveLiveOrder", false).apply();
            cardLiveTrackingHome.setVisibility(View.GONE);
            return;
        }

        long orderId = prefs.getLong("activeOrderId", 103L);
        String pharmacyName = prefs.getString("activePharmacyName", "Apollo Metro Pharmacy");
        String price = prefs.getString("activePrice", "₹90.00");
        String address = prefs.getString("activeDeliveryAddress", "Apt 402, Green Avenue, Sector 4, Metro City");
        String status = prefs.getString("activeStatus", "IN TRANSIT");

        int remainingMinutes = (int) Math.max(2, 11 - elapsedMinutes);
        if (tvHomeTrackingEta != null) {
            tvHomeTrackingEta.setText(remainingMinutes + " mins");
        }

        if (pbHomeTracking != null) {
            int progress = Math.min(95, (int) (35 + elapsedMinutes * 5));
            pbHomeTracking.setProgress(progress);
        }

        if (tvHomeTrackingStatus != null) {
            tvHomeTrackingStatus.setText("Your order from " + pharmacyName + " is on the way 🤘");
        }

        cardLiveTrackingHome.setVisibility(View.VISIBLE);

        cardLiveTrackingHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DeliveryTrackingActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("pharmacyName", pharmacyName);
            intent.putExtra("price", price);
            intent.putExtra("status", status);
            intent.putExtra("eta", remainingMinutes + " mins");
            intent.putExtra("deliveryAddress", address);
            startActivity(intent);
        });

        if (btnDismissHomeTracking != null) {
            btnDismissHomeTracking.setOnClickListener(v -> {
                prefs.edit().putBoolean("hasActiveLiveOrder", false).apply();
                cardLiveTrackingHome.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Live delivery banner dismissed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserHeader();
        updateLiveTrackingCard();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}