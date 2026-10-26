package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.adapters.SearchAdapter;
import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.models.Pharmacy;
import com.example.pharmago.models.SearchItem;
import com.example.pharmago.services.BackendService;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    EditText etSearchPharmacy;
    Button btnSearch;
    RecyclerView recyclerView;
    LinearLayout pharmacyContainer;
    ProgressBar progressBar;
    ChipGroup chipGroupFilters;
    SearchAdapter searchAdapter;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requirement 2: setContentView strictly BEFORE any findViewById
        setContentView(R.layout.activity_search);

        // Cross-referenced IDs against activity_search.xml
        etSearchPharmacy = findViewById(R.id.etSearchPharmacy);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerView = findViewById(R.id.recyclerView);
        pharmacyContainer = findViewById(R.id.pharmacyContainer);
        progressBar = findViewById(R.id.progressBar);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        // Requirement 3: Ensure RecyclerView is initialized with a LinearLayoutManager
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setNestedScrollingEnabled(false);
        }

        // Requirement 3: Distinct Sections (Pharmacies/Shops, Hospitals, Medicines)
        List<SearchItem> mockItems = createMockSearchData();
        searchAdapter = new SearchAdapter(mockItems, item -> {
            if (item == null) return;
            // Requirement Part 1.3: Tapping a medicine or pharmacy card navigates directly to PharmacyDetailsActivity passing pharmacy_id
            Intent intent = new Intent(SearchActivity.this, PharmacyDetailsActivity.class);
            intent.putExtra("pharmacy_id", item.getPharmacyId());
            intent.putExtra("pharmacyId", item.getPharmacyId());
            intent.putExtra("pharmacyName", item.getPharmacyName());
            intent.putExtra("selectedMedicineName", item.getMedicineName());
            intent.putExtra("selectedMedicinePrice", item.getPrice());
            intent.putExtra("selectedMedicineDosage", item.getDosage());
            intent.putExtra("selectedMedicineRx", item.isRequiresPrescription());
            startActivity(intent);
        });

        if (recyclerView != null) {
            recyclerView.setAdapter(searchAdapter);
        }

        // Horizontal Filter Tabs listener
        if (chipGroupFilters != null) {
            chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || searchAdapter == null) return;
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) {
                    searchAdapter.filterByType("ALL");
                } else if (id == R.id.chipMedicines) {
                    searchAdapter.filterByType("MEDICINE");
                } else if (id == R.id.chipPharmacies) {
                    searchAdapter.filterByType("PHARMACY");
                } else if (id == R.id.chipHospitals) {
                    searchAdapter.filterByType("HOSPITAL");
                } else if (id == R.id.chipInStock) {
                    searchAdapter.filter("In Stock");
                }
            });
        }

        // Requirement 3: TextWatcher listener that filters this mock list locally
        if (etSearchPharmacy != null) {
            etSearchPharmacy.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchAdapter != null) {
                        searchAdapter.filter(s != null ? s.toString() : "");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Requirement 2: Add null-checks and basic client-side input validations
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                String searchText = etSearchPharmacy != null && etSearchPharmacy.getText() != null
                        ? etSearchPharmacy.getText().toString().trim() : "";

                if (searchText.isEmpty()) {
                    if (etSearchPharmacy != null) {
                        etSearchPharmacy.setError("Please enter a medicine or pharmacy name");
                        etSearchPharmacy.requestFocus();
                    }
                    if (searchAdapter != null) {
                        searchAdapter.filter("");
                    }
                    return;
                }

                if (searchAdapter != null) {
                    searchAdapter.filter(searchText);
                }
                fetchBackendDataLive(searchText);
            });
        }

        // Check if query was passed from HomeActivity
        String incomingQuery = getIntent().getStringExtra("searchQuery");
        if (incomingQuery != null && !incomingQuery.trim().isEmpty()) {
            if (etSearchPharmacy != null) {
                etSearchPharmacy.setText(incomingQuery);
            }
            if (searchAdapter != null) {
                searchAdapter.filter(incomingQuery);
            }
            fetchBackendDataLive(incomingQuery);
        } else {
            // Load live inventory from backend asynchronously
            fetchBackendDataLive("");
        }
    }

    private List<SearchItem> createMockSearchData() {
        List<SearchItem> list = new ArrayList<>();
        // 1. Medicines
        list.add(new SearchItem(
                1L, 1L, "Paracetamol 500mg", "500 mg Tablet • Strip of 10",
                "Apollo Metro Pharmacy", "₹45.00", "Analgesic / Fever",
                "Fast relief for headaches, musculoskeletal pain and fever",
                true, false, "MEDICINE"
        ));
        list.add(new SearchItem(
                2L, 1L, "Amoxicillin 250mg", "250 mg Capsule • Strip of 6",
                "Apollo Metro Pharmacy", "₹120.00", "Antibiotic",
                "Broad-spectrum treatment for bacterial respiratory infections",
                true, true, "MEDICINE"
        ));
        list.add(new SearchItem(
                3L, 2L, "Cetirizine 10mg", "10 mg Tablet • Strip of 10",
                "MedPlus Wellness Store", "₹35.00", "Antihistamine",
                "24-hour relief from allergies, sneezing and runny nose",
                true, false, "MEDICINE"
        ));
        list.add(new SearchItem(
                4L, 2L, "Ibuprofen 400mg", "400 mg Tablet • Strip of 10",
                "MedPlus Wellness Store", "₹60.00", "NSAID",
                "Anti-inflammatory pain reliever for dental and body pain",
                false, false, "MEDICINE"
        ));
        list.add(new SearchItem(
                5L, 3L, "Metformin 500mg", "500 mg Tablet • Strip of 15",
                "CarePoint Chemist", "₹85.00", "Diabetes Care",
                "First-line medication for type 2 diabetes management",
                true, true, "MEDICINE"
        ));

        // 2. Pharmacies / Shops
        list.add(new SearchItem(
                101L, 1L, "Apollo Metro Pharmacy", "Full Retail Pharmacy & Chemist",
                "Apollo Metro Pharmacy", "Min ₹30.00", "Retail Pharmacy",
                "Verified 24/7 store with over 5,000 verified medicines in stock",
                true, false, "PHARMACY"
        ));
        list.add(new SearchItem(
                102L, 2L, "MedPlus Wellness Store", "Wellness & Prescription Chemist",
                "MedPlus Wellness Store", "Min ₹25.00", "Retail Pharmacy",
                "Full catalog with doorstep delivery & takeaway options",
                true, false, "PHARMACY"
        ));

        // 3. Hospitals
        list.add(new SearchItem(
                201L, 3L, "City Care Multispeciality Hospital", "Central Hospital Dispensary",
                "City Care Hospital Pharmacy", "Min ₹50.00", "Hospital",
                "24/7 Hospital emergency counter and specialized oncology/cardio dispensary",
                true, false, "HOSPITAL"
        ));
        list.add(new SearchItem(
                202L, 4L, "Apollo Super Speciality Hospital", "Campus Pharmacy & Dispensary",
                "Apollo Hospital Dispensary", "Min ₹40.00", "Hospital",
                "State-of-the-art medical dispensary with cold-chain storage for biologics",
                true, false, "HOSPITAL"
        ));
        return list;
    }

    private void loadBackendMedicines(String query) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        BackendService.getInstance(this).searchMedicines(query, new DbCallback<List<InventoryItem>>() {
            @Override
            public void onSuccess(List<InventoryItem> inventoryItems) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (inventoryItems != null && !inventoryItems.isEmpty()) {
                    // Merge live medicines with existing mock hospitals/pharmacies
                    List<SearchItem> mergedItems = new ArrayList<>(createMockSearchData());
                    // Replace only MEDICINE entries with live data
                    mergedItems.removeIf(item -> "MEDICINE".equalsIgnoreCase(item.getItemType()));
                    for (InventoryItem inv : inventoryItems) {
                        mergedItems.add(new SearchItem(
                                inv.getId(),
                                1L,
                                inv.getBrandName(),
                                inv.getDosage() != null && !inv.getDosage().isEmpty() ? inv.getDosage() : "500 mg Tablet",
                                "Hospital Central Pharmacy",
                                String.format(Locale.getDefault(), "₹%.2f", inv.getUnitPrice()),
                                inv.getCategory() != null ? inv.getCategory() : "Medication",
                                "Stock: " + inv.getStockQty() + " units • Batch: " + (inv.getBatchNo() != null ? inv.getBatchNo() : "A101"),
                                inv.getStockQty() > 0,
                                inv.getStockQty() < 10,
                                "MEDICINE"
                        ));
                    }
                    if (searchAdapter != null) {
                        searchAdapter.updateData(mergedItems);
                    }
                }
                // On empty result keep mock data as-is
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                // Keep mock data (including hospitals) intact
            }
        });
    }

    private void fetchBackendDataLive(String query) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        try {
            apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);
            apiService.getAllMedicines().enqueue(new Callback<List<com.example.pharmago.models.Medicine>>() {
                @Override
                public void onResponse(Call<List<com.example.pharmago.models.Medicine>> call, Response<List<com.example.pharmago.models.Medicine>> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        // Merge live medicines with existing mock hospitals/pharmacies
                        List<SearchItem> mergedItems = new ArrayList<>(createMockSearchData());
                        mergedItems.removeIf(item -> "MEDICINE".equalsIgnoreCase(item.getItemType()));
                        for (com.example.pharmago.models.Medicine m : response.body()) {
                            mergedItems.add(new SearchItem(
                                    m.getId() != null ? m.getId() : 1L,
                                    m.getName(),
                                    "Apollo Hospital Pharmacy",
                                    m.getPrice(),
                                    m.getCategory() != null ? m.getCategory() : "Medication",
                                    m.getDescription() != null ? m.getDescription() : "In Stock",
                                    m.isRequiresPrescription()
                            ));
                        }
                        if (searchAdapter != null) {
                            searchAdapter.updateData(mergedItems);
                            if (query != null && !query.trim().isEmpty()) {
                                searchAdapter.filter(query);
                            }
                        }
                    } else {
                        loadBackendMedicines(query);
                    }
                }

                @Override
                public void onFailure(Call<List<com.example.pharmago.models.Medicine>> call, Throwable t) {
                    loadBackendMedicines(query);
                }
            });
        } catch (Exception e) {
            loadBackendMedicines(query);
        }
    }

    private void fetchBackendPharmaciesSafely() {
        try {
            apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);
            apiService.getAllPharmacies().enqueue(new Callback<List<Pharmacy>>() {
                @Override
                public void onResponse(Call<List<Pharmacy>> call, Response<List<Pharmacy>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        List<SearchItem> backendItems = new ArrayList<>(createMockSearchData());
                        for (Pharmacy p : response.body()) {
                            if (p != null && p.getName() != null) {
                                backendItems.add(new SearchItem(
                                        p.getId() != null ? p.getId() : 99L,
                                        "General Medicines",
                                        p.getName(),
                                        "From ₹50.00",
                                        "Pharmacy Store",
                                        p.getAddress() != null ? p.getAddress() : "Active Pharmacy",
                                        false
                                ));
                            }
                        }
                        if (searchAdapter != null) {
                            searchAdapter.updateData(backendItems);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Pharmacy>> call, Throwable t) {
                    // Safe failure handling: mock data remains visible and fully interactive
                }
            });
        } catch (Exception e) {
            // Ignored to guarantee zero-crash execution
        }
    }
}