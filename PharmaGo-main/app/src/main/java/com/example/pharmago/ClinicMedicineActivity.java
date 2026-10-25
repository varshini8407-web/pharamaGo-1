package com.example.pharmago;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pharmago.adapters.InventoryAdapter;
import com.example.pharmago.database.DatabaseConnection;
import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.utils.PharmacistNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClinicMedicineActivity extends AppCompatActivity {

    private ImageView btnBackInventory, btnHeaderAddMedicine, btnClearSearchInventory;
    private TextView tvInventorySubtitle;
    private EditText etSearchInventory;
    private ChipGroup chipGroupInventoryCategory;
    private SwipeRefreshLayout swipeRefreshInventory;
    private RecyclerView rvClinicInventory;
    private View layoutEmptyInventory;
    private ProgressBar pbInventoryLoading;
    private ExtendedFloatingActionButton fabAddMedicine;
    private BottomNavigationView pharmacistBottomNav;

    private InventoryAdapter inventoryAdapter;
    private DatabaseConnection databaseConnection;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String currentSearch = "";
    private String currentCategory = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_medicine);

        databaseConnection = DatabaseConnection.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        loadInventory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pharmacistBottomNav != null) {
            pharmacistBottomNav.setSelectedItemId(R.id.nav_pharm_inventory);
        }
        loadInventory();
    }

    private void initViews() {
        btnBackInventory = findViewById(R.id.btnBackInventory);
        btnHeaderAddMedicine = findViewById(R.id.btnHeaderAddMedicine);
        btnClearSearchInventory = findViewById(R.id.btnClearSearchInventory);
        tvInventorySubtitle = findViewById(R.id.tvInventorySubtitle);

        etSearchInventory = findViewById(R.id.etSearchInventory);
        chipGroupInventoryCategory = findViewById(R.id.chipGroupInventoryCategory);
        swipeRefreshInventory = findViewById(R.id.swipeRefreshInventory);
        rvClinicInventory = findViewById(R.id.rvInventoryMedicines);
        layoutEmptyInventory = findViewById(R.id.layoutEmptyInventory);
        pbInventoryLoading = findViewById(R.id.pbInventoryLoading);
        fabAddMedicine = findViewById(R.id.fabAddMedicine);
        pharmacistBottomNav = findViewById(R.id.pharmacistBottomNav);

        rvClinicInventory.setLayoutManager(new LinearLayoutManager(this));
        inventoryAdapter = new InventoryAdapter(this, new ArrayList<>(), (item, newStock) -> {
            updateStockInDatabase(item.getMedicineId(), newStock, item.getMedicineName());
        });
        rvClinicInventory.setAdapter(inventoryAdapter);

        PharmacistNavigationHelper.setupBottomNavigation(this, pharmacistBottomNav, R.id.nav_pharm_inventory);
    }

    private void setupListeners() {
        btnBackInventory.setOnClickListener(v -> finish());
        btnHeaderAddMedicine.setOnClickListener(v -> showAddMedicineBottomSheet());
        fabAddMedicine.setOnClickListener(v -> showAddMedicineBottomSheet());

        swipeRefreshInventory.setOnRefreshListener(this::loadInventory);

        btnClearSearchInventory.setOnClickListener(v -> {
            etSearchInventory.setText("");
            btnClearSearchInventory.setVisibility(View.GONE);
        });

        etSearchInventory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                btnClearSearchInventory.setVisibility(currentSearch.isEmpty() ? View.GONE : View.VISIBLE);
                loadInventory();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupInventoryCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCatAntibiotics)) {
                currentCategory = "Antibiotics";
            } else if (checkedIds.contains(R.id.chipCatPain)) {
                currentCategory = "Pain Relief";
            } else if (checkedIds.contains(R.id.chipCatCardio)) {
                currentCategory = "Cardiovascular";
            } else if (checkedIds.contains(R.id.chipCatVitamins)) {
                currentCategory = "Vitamins";
            } else if (checkedIds.contains(R.id.chipCatLowStock)) {
                currentCategory = "LOW STOCK";
            } else {
                currentCategory = "ALL";
            }
            loadInventory();
        });
    }

    private void loadInventory() {
        if (!swipeRefreshInventory.isRefreshing()) {
            pbInventoryLoading.setVisibility(View.VISIBLE);
        }

        com.example.pharmago.services.BackendService.getInstance(this).getInventory(
                new com.example.pharmago.database.DbCallback<List<InventoryItem>>() {
                    @Override
                    public void onSuccess(List<InventoryItem> items) {
                        pbInventoryLoading.setVisibility(View.GONE);
                        swipeRefreshInventory.setRefreshing(false);
                        if (items != null && !items.isEmpty()) {
                            inventoryAdapter.updateData(items);
                            layoutEmptyInventory.setVisibility(View.GONE);
                            rvClinicInventory.setVisibility(View.VISIBLE);
                            tvInventorySubtitle.setText(items.size() + " pharmaceutical items registered");
                        } else {
                            loadLocalInventoryFallback();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        loadLocalInventoryFallback();
                    }
                }
        );
    }

    private void loadLocalInventoryFallback() {
        executorService.execute(() -> {
            List<InventoryItem> items = databaseConnection.getAllInventory(currentSearch, currentCategory);
            mainHandler.post(() -> {
                pbInventoryLoading.setVisibility(View.GONE);
                swipeRefreshInventory.setRefreshing(false);
                inventoryAdapter.updateData(items);

                boolean empty = items.isEmpty();
                layoutEmptyInventory.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvClinicInventory.setVisibility(empty ? View.GONE : View.VISIBLE);

                tvInventorySubtitle.setText(items.size() + " pharmaceutical items registered");
            });
        });
    }

    private void updateStockInDatabase(long medicineId, int newStock, String medicineName) {
        com.example.pharmago.services.BackendService.getInstance(this).updateStock(
                medicineId,
                newStock,
                new com.example.pharmago.database.DbCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(ClinicMedicineActivity.this, medicineName + " stock updated to " + newStock + " units via MySQL", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ClinicMedicineActivity.this, "Stock update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showAddMedicineBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medicine, null);
        dialog.setContentView(view);

        TextInputEditText etName = view.findViewById(R.id.etAddMedName);
        TextInputEditText etDosage = view.findViewById(R.id.etAddMedDosage);
        TextInputEditText etCategory = view.findViewById(R.id.etAddMedCategory);
        TextInputEditText etPrice = view.findViewById(R.id.etAddMedPrice);
        TextInputEditText etStock = view.findViewById(R.id.etAddMedStock);
        TextInputEditText etBatch = view.findViewById(R.id.etAddMedBatch);
        TextInputEditText etExpiry = view.findViewById(R.id.etAddMedExpiry);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancelAddMed);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmAddMed);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
            String cat = etCategory.getText() != null ? etCategory.getText().toString().trim() : "General";
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";
            String batch = etBatch.getText() != null ? etBatch.getText().toString().trim() : "BATCH-" + System.currentTimeMillis() % 10000;
            String expiry = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "12/2028";

            if (name.isEmpty()) {
                etName.setError("Medicine name is required");
                etName.requestFocus();
                return;
            }

            if (dosage.isEmpty()) {
                dosage = "Standard Packaging";
            }

            double price = 50.00;
            try {
                if (!priceStr.isEmpty()) price = Double.parseDouble(priceStr);
            } catch (Exception ignored) {}

            int stock = 20;
            try {
                if (!stockStr.isEmpty()) stock = Integer.parseInt(stockStr);
            } catch (Exception ignored) {}

            long newId = System.currentTimeMillis() % 90000 + 10000;
            InventoryItem newItem = new InventoryItem(newId, name, dosage, stock, price, cat, batch, expiry, 0);

            btnConfirm.setEnabled(false);
            executorService.execute(() -> {
                boolean success = databaseConnection.addInventoryItem(newItem);
                mainHandler.post(() -> {
                    dialog.dismiss();
                    if (success) {
                        Toast.makeText(this, "Medicine registered in hospital inventory!", Toast.LENGTH_SHORT).show();
                        loadInventory();
                    } else {
                        Toast.makeText(this, "Failed to register medicine", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
