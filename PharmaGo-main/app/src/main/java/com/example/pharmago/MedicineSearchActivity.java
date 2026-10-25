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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.models.Medicine;
import com.example.pharmago.services.BackendService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicineSearchActivity extends AppCompatActivity {

    EditText etSearchMedicine;
    Button btnSearchMedicine;
    LinearLayout medicineContainer;
    ProgressBar progressBar;
    String pharmacyName;
    Long pharmacyId;
    ApiService apiService;
    List<Medicine> allMedicinesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_search);

        etSearchMedicine = findViewById(R.id.etSearchMedicine);
        btnSearchMedicine = findViewById(R.id.btnSearchMedicine);
        medicineContainer = findViewById(R.id.medicineContainer);
        progressBar = findViewById(R.id.progressBar);

        pharmacyName = getIntent().getStringExtra("pharmacyName");
        pharmacyId = getIntent().getLongExtra("pharmacyId", -1L);

        TextView pharmacyTitle = findViewById(R.id.tvPharmacyName);
        if (pharmacyTitle != null) {
            pharmacyTitle.setText(pharmacyName != null ? pharmacyName : "All Medicines");
        }

        // Initialize with 5 mock medicines immediately
        allMedicinesList = createMockMedicines();
        displayMedicines(allMedicinesList);

        if (btnSearchMedicine != null) {
            btnSearchMedicine.setOnClickListener(v -> {
                String searchText = etSearchMedicine != null && etSearchMedicine.getText() != null
                        ? etSearchMedicine.getText().toString().trim() : "";
                filterMedicines(searchText);
            });
        }

        if (etSearchMedicine != null) {
            etSearchMedicine.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterMedicines(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadMedicinesFromServerSafely();
    }

    private List<Medicine> createMockMedicines() {
        List<Medicine> list = new ArrayList<>();

        Medicine m1 = new Medicine();
        m1.setId(1L);
        m1.setName("Paracetamol 500mg");
        m1.setDescription("Effective fever and mild to moderate pain relief.");
        m1.setCategory("Pain Relief");
        m1.setPrice("₹45.00");
        m1.setRequiresPrescription(false);
        list.add(m1);

        Medicine m2 = new Medicine();
        m2.setId(2L);
        m2.setName("Amoxicillin 250mg");
        m2.setDescription("Antibiotic used to treat a wide variety of bacterial infections.");
        m2.setCategory("Antibiotic");
        m2.setPrice("₹120.00");
        m2.setRequiresPrescription(true);
        list.add(m2);

        Medicine m3 = new Medicine();
        m3.setId(3L);
        m3.setName("Cetirizine 10mg");
        m3.setDescription("Fast-acting antihistamine for hay fever, conjunctivitis and hives.");
        m3.setCategory("Antihistamine");
        m3.setPrice("₹35.00");
        m3.setRequiresPrescription(false);
        list.add(m3);

        Medicine m4 = new Medicine();
        m4.setId(4L);
        m4.setName("Ibuprofen 400mg");
        m4.setDescription("NSAID reducing hormones that cause inflammation and pain in the body.");
        m4.setCategory("NSAID");
        m4.setPrice("₹60.00");
        m4.setRequiresPrescription(false);
        list.add(m4);

        Medicine m5 = new Medicine();
        m5.setId(5L);
        m5.setName("Metformin 500mg");
        m5.setDescription("Antidiabetic medicine treating high blood sugar in type 2 diabetes.");
        m5.setCategory("Antidiabetic");
        m5.setPrice("₹85.00");
        m5.setRequiresPrescription(true);
        list.add(m5);

        return list;
    }

    private void filterMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayMedicines(allMedicinesList);
            return;
        }

        String q = query.toLowerCase().trim();
        List<Medicine> filtered = new ArrayList<>();
        for (Medicine m : allMedicinesList) {
            if (m != null) {
                boolean matchName = m.getName() != null && m.getName().toLowerCase().contains(q);
                boolean matchDesc = m.getDescription() != null && m.getDescription().toLowerCase().contains(q);
                boolean matchCategory = m.getCategory() != null && m.getCategory().toLowerCase().contains(q);
                if (matchName || matchDesc || matchCategory) {
                    filtered.add(m);
                }
            }
        }
        displayMedicines(filtered);
    }

    private void loadMedicinesFromServerSafely() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        attemptRetrofitLoad();
    }

    private void attemptRetrofitLoad() {
        try {
            apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);
            Call<List<Medicine>> call = apiService.getAllMedicines();
            call.enqueue(new Callback<List<Medicine>>() {
                @Override
                public void onResponse(@NonNull Call<List<Medicine>> call, @NonNull Response<List<Medicine>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        allMedicinesList = response.body();
                        displayMedicines(allMedicinesList);
                    } else {
                        fallbackBackendServiceLoad();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Medicine>> call, @NonNull Throwable t) {
                    fallbackBackendServiceLoad();
                }
            });
        } catch (Exception e) {
            fallbackBackendServiceLoad();
        }
    }

    private void fallbackBackendServiceLoad() {
        BackendService.getInstance(this).searchMedicines("", new DbCallback<List<InventoryItem>>() {
            @Override
            public void onSuccess(List<InventoryItem> items) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (items != null && !items.isEmpty()) {
                    List<Medicine> list = new ArrayList<>();
                    for (InventoryItem inv : items) {
                        Medicine m = new Medicine();
                        m.setId(inv.getId());
                        m.setName(inv.getBrandName() + (inv.getDosage() != null && !inv.getDosage().isEmpty() ? " " + inv.getDosage() : ""));
                        m.setDescription("Batch: " + inv.getBatchNo() + " • In Stock: " + inv.getStockQty() + " units");
                        m.setCategory(inv.getCategory() != null ? inv.getCategory() : "Medication");
                        m.setPrice(String.format(Locale.getDefault(), "₹%.2f", inv.getUnitPrice()));
                        m.setRequiresPrescription(inv.getStockQty() < 10);
                        list.add(m);
                    }
                    allMedicinesList = list;
                    displayMedicines(allMedicinesList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayMedicines(List<Medicine> medicines) {
        if (medicineContainer == null) return;
        medicineContainer.removeAllViews();

        if (medicines == null || medicines.isEmpty()) {
            TextView noResult = new TextView(this);
            noResult.setText("No medicines found matching your search.");
            noResult.setTextSize(16);
            noResult.setPadding(20, 40, 20, 40);
            noResult.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            medicineContainer.addView(noResult);
            return;
        }

        for (Medicine medicine : medicines) {
            if (medicine != null) {
                addMedicineCard(medicine);
            }
        }
    }

    private void addMedicineCard(Medicine medicine) {
        View card = getLayoutInflater().inflate(R.layout.item_medicine, medicineContainer, false);

        TextView name = card.findViewById(R.id.tvMedicineName);
        TextView desc = card.findViewById(R.id.tvMedicineDesc);
        TextView category = card.findViewById(R.id.tvMedicineCategory);
        TextView prescription = card.findViewById(R.id.tvPrescriptionStatus);
        Button selectButton = card.findViewById(R.id.btnSelectMedicine);

        if (name != null) {
            name.setText(medicine.getName() + (medicine.getPrice() != null ? " • " + medicine.getPrice() : ""));
        }
        if (desc != null) {
            desc.setText(medicine.getDescription());
        }
        if (category != null) {
            category.setText(String.format("Category: %s", medicine.getCategory() != null ? medicine.getCategory() : "General"));
        }

        if (prescription != null) {
            if (medicine.isRequiresPrescription()) {
                prescription.setText("⚠️ Prescription Required");
                prescription.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                prescription.setText("✅ Over the Counter");
                prescription.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        }

        if (selectButton != null) {
            selectButton.setOnClickListener(v -> {
                Intent intent = new Intent(MedicineSearchActivity.this, OrderTypeActivity.class);
                intent.putExtra("medicineId", medicine.getId());
                intent.putExtra("medicineName", medicine.getName());
                intent.putExtra("pharmacyName", pharmacyName != null ? pharmacyName : "Selected Pharmacy");
                intent.putExtra("pharmacyId", pharmacyId);
                intent.putExtra("price", medicine.getPrice());
                intent.putExtra("requiresPrescription", medicine.isRequiresPrescription());
                startActivity(intent);
            });
        }

        medicineContainer.addView(card);
    }
}