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

        // Requirement 3: Inject 3-5 items of hardcoded mock data with dummy prices and store names
        List<SearchItem> mockItems = createMockSearchData();
        searchAdapter = new SearchAdapter(mockItems, item -> {
            if (item == null) return;
            Intent intent = new Intent(SearchActivity.this, OrderTypeActivity.class);
            intent.putExtra("medicineId", item.getId());
            intent.putExtra("medicineName", item.getMedicineName());
            intent.putExtra("pharmacyName", item.getPharmacyName());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("requiresPrescription", item.isRequiresPrescription());
            startActivity(intent);
        });

        if (recyclerView != null) {
            recyclerView.setAdapter(searchAdapter);
        }

        // Horizontal Chip Filter listener
        if (chipGroupFilters != null) {
            chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || searchAdapter == null) return;
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) {
                    searchAdapter.filter("");
                } else if (id == R.id.chipMedicines) {
                    searchAdapter.filter("mg");
                } else if (id == R.id.chipPharmacies) {
                    searchAdapter.filter("Pharmacy");
                } else if (id == R.id.chipHospitals) {
                    searchAdapter.filter("Chemist");
                } else if (id == R.id.chipInStock) {
                    searchAdapter.filter("Paracetamol");
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
        list.add(new SearchItem(
                1L,
                "Paracetamol 500mg",
                "Apollo Pharmacy",
                "₹45.00",
                "Analgesic / Fever",
                "Fast relief for headaches, musculoskeletal pain and fever",
                false
        ));
        list.add(new SearchItem(
                2L,
                "Amoxicillin 250mg",
                "MedPlus Wellness",
                "₹120.00",
                "Antibiotic",
                "Broad-spectrum treatment for bacterial respiratory infections",
                true
        ));
        list.add(new SearchItem(
                3L,
                "Cetirizine 10mg",
                "CarePoint Chemist",
                "₹35.00",
                "Antihistamine",
                "24-hour relief from allergies, sneezing and runny nose",
                false
        ));
        list.add(new SearchItem(
                4L,
                "Ibuprofen 400mg",
                "City Meds",
                "₹60.00",
                "NSAID",
                "Anti-inflammatory pain reliever for dental and body pain",
                false
        ));
        list.add(new SearchItem(
                5L,
                "Metformin 500mg",
                "HealthLine Pharmacy",
                "₹85.00",
                "Diabetes Care",
                "First-line medication for type 2 diabetes management",
                true
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
                    List<SearchItem> liveItems = new ArrayList<>();
                    for (InventoryItem inv : inventoryItems) {
                        liveItems.add(new SearchItem(
                                inv.getId(),
                                inv.getBrandName() + (inv.getDosage() != null && !inv.getDosage().isEmpty() ? " " + inv.getDosage() : ""),
                                "Hospital Central Pharmacy",
                                String.format(Locale.getDefault(), "₹%.2f", inv.getUnitPrice()),
                                inv.getDosage() != null && !inv.getDosage().isEmpty() ? inv.getDosage() : "Medication",
                                "Stock: " + inv.getStockQty() + " units • Batch: " + (inv.getBatchNo() != null ? inv.getBatchNo() : "A101"),
                                inv.getStockQty() < 10
                        ));
                    }
                    if (searchAdapter != null) {
                        searchAdapter.updateData(liveItems);
                    }
                } else {
                    fetchBackendPharmaciesSafely();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                fetchBackendPharmaciesSafely();
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
                        List<SearchItem> items = new ArrayList<>();
                        for (com.example.pharmago.models.Medicine m : response.body()) {
                            items.add(new SearchItem(
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
                            searchAdapter.updateData(items);
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