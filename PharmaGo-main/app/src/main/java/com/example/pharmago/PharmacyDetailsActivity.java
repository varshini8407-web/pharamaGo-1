package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.models.Medicine;
import com.example.pharmago.utils.CartManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pharmacy Details & Catalog Activity.
 * Requirement Part 1.2 & 1.3:
 * - Single-Pharmacy Cart Enforcement via CartManager
 * - Displays store's full catalog with badges (Stock, Rx Required / OTC)
 * - Flags cart if Rx-required item added
 * - Direct navigation to PrescriptionUploadActivity (if Rx required) or Checkout
 */
public class PharmacyDetailsActivity extends AppCompatActivity {

    private int pharmacyId = 1;
    private String pharmacyName = "Apollo Metro Pharmacy";
    private TextView tvPharmacyDetailName, tvPharmacyDetailAddress;
    private TextView tvCartItemCount, tvCartTotalAmount;
    private LinearLayout layoutRxWarning;
    private MaterialButton btnProceedCheckout;
    private RecyclerView rvPharmacyCatalog;
    private CatalogAdapter catalogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_details);

        // Parse extras
        long pId = getIntent().getLongExtra("pharmacy_id", getIntent().getLongExtra("pharmacyId", 1L));
        pharmacyId = (int) pId;
        String name = getIntent().getStringExtra("pharmacyName");
        if (name != null && !name.trim().isEmpty()) {
            pharmacyName = name;
        }

        // Initialize Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarPharmacy);
        if (toolbar != null) {
            toolbar.setTitle(pharmacyName);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvPharmacyDetailName = findViewById(R.id.tvPharmacyDetailName);
        tvPharmacyDetailAddress = findViewById(R.id.tvPharmacyDetailAddress);
        tvCartItemCount = findViewById(R.id.tvCartItemCount);
        tvCartTotalAmount = findViewById(R.id.tvCartTotalAmount);
        layoutRxWarning = findViewById(R.id.layoutRxWarning);
        btnProceedCheckout = findViewById(R.id.btnProceedCheckout);
        rvPharmacyCatalog = findViewById(R.id.rvPharmacyCatalog);

        if (tvPharmacyDetailName != null) {
            tvPharmacyDetailName.setText(pharmacyName);
        }
        if (tvPharmacyDetailAddress != null) {
            tvPharmacyDetailAddress.setText("Pharmacy ID #" + pharmacyId + " • Verified Healthcare Partner");
        }

        // Setup Catalog RecyclerView
        if (rvPharmacyCatalog != null) {
            rvPharmacyCatalog.setLayoutManager(new LinearLayoutManager(this));
            rvPharmacyCatalog.setNestedScrollingEnabled(false);
            List<Medicine> catalog = generatePharmacyCatalog(pharmacyId);
            catalogAdapter = new CatalogAdapter(catalog, this::onAddToCartClicked);
            rvPharmacyCatalog.setAdapter(catalogAdapter);
        }

        // Setup Checkout Button
        if (btnProceedCheckout != null) {
            btnProceedCheckout.setOnClickListener(v -> {
                CartManager cart = CartManager.getInstance();
                if (cart.getItemCount() == 0) {
                    Toast.makeText(this, "Your cart is empty. Please add items to proceed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cart.isRequiresPrescription()) {
                    // Navigate to prescription upload step
                    Intent intent = new Intent(PharmacyDetailsActivity.this, PrescriptionUploadActivity.class);
                    intent.putExtra("pharmacy_id", (long) pharmacyId);
                    intent.putExtra("pharmacyName", pharmacyName);
                    intent.putExtra("orderType", "TAKEAWAY");
                    startActivity(intent);
                } else {
                    // Regular order checkout flow
                    Intent intent = new Intent(PharmacyDetailsActivity.this, OrderTypeActivity.class);
                    intent.putExtra("pharmacyId", (long) pharmacyId);
                    intent.putExtra("pharmacyName", pharmacyName);
                    intent.putExtra("price", String.format(Locale.getDefault(), "₹%.2f", cart.getTotalAmount()));
                    startActivity(intent);
                }
            });
        }

        updateBottomCartBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomCartBar();
    }

    private void onAddToCartClicked(Medicine medicine) {
        if (medicine == null) return;
        CartManager.getInstance().addToCart(this, medicine, pharmacyId, pharmacyName, (totalItems, totalAmount) -> {
            updateBottomCartBar();
            Toast.makeText(this, "Added " + medicine.getName() + " to cart", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBottomCartBar() {
        CartManager cart = CartManager.getInstance();
        int count = cart.getItemCount();
        double total = cart.getTotalAmount();

        if (tvCartItemCount != null) {
            tvCartItemCount.setText(count + (count == 1 ? " item in cart" : " items in cart"));
        }
        if (tvCartTotalAmount != null) {
            tvCartTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        }

        if (layoutRxWarning != null) {
            layoutRxWarning.setVisibility(cart.isRequiresPrescription() ? View.VISIBLE : View.GONE);
        }
    }

    private List<Medicine> generatePharmacyCatalog(int pId) {
        List<Medicine> list = new ArrayList<>();
        list.add(new Medicine(1L, "Paracetamol 500mg", "₹45.00", "Analgesic", "Fast relief for pain and fever", false));
        list.add(new Medicine(2L, "Amoxicillin 250mg", "₹120.00", "Antibiotic", "Broad-spectrum antibacterial capsules", true));
        list.add(new Medicine(3L, "Cetirizine 10mg", "₹35.00", "Antihistamine", "24-hr allergy and rhinitis relief", false));
        list.add(new Medicine(4L, "Metformin 500mg", "₹85.00", "Diabetes Care", "Type 2 diabetes glycemic control", true));
        list.add(new Medicine(5L, "Ibuprofen 400mg", "₹60.00", "NSAID", "Anti-inflammatory pain reliever", false));
        list.add(new Medicine(6L, "Azithromycin 500mg", "₹140.00", "Antibiotic", "Short-course antibiotic treatment", true));
        list.add(new Medicine(7L, "Pantoprazole 40mg", "₹95.00", "Gastro Care", "Acid reflux and GERD inhibitor", false));
        list.add(new Medicine(8L, "Atorvastatin 10mg", "₹110.00", "Cardio Care", "Cholesterol and lipid regulator", true));
        return list;
    }

    // Inner RecyclerView Adapter for Catalog
    private static class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.ViewHolder> {
        private final List<Medicine> items;
        private final OnAddClickListener listener;

        interface OnAddClickListener {
            void onAdd(Medicine medicine);
        }

        CatalogAdapter(List<Medicine> items, OnAddClickListener listener) {
            this.items = items != null ? items : new ArrayList<>();
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Medicine m = items.get(position);
            if (m == null) return;

            holder.tvMedicineName.setText(m.getName());
            holder.tvMedicineDosage.setText(m.getDescription() != null ? m.getDescription() : "Standard Dosage");
            holder.tvMedicinePrice.setText(m.getPrice());
            holder.tvMedicineDesc.setText("Category: " + (m.getCategory() != null ? m.getCategory() : "General"));

            // Stock Badge: In Stock
            holder.tvStockBadge.setText("In Stock");
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_mint));
            holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.secondary_container));

            // Prescription Badge
            if (m.isRequiresPrescription()) {
                holder.tvPrescriptionStatus.setText("Rx Required");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_warning_container));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.warning_container));
            } else {
                holder.tvPrescriptionStatus.setText("Over the Counter");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.neutral_muted));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.background_surface));
            }

            holder.btnSelectMedicine.setText("+ Add to Cart");
            holder.btnSelectMedicine.setOnClickListener(v -> {
                if (listener != null) listener.onAdd(m);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMedicineName, tvMedicineDosage, tvMedicinePrice, tvMedicineDesc, tvStockBadge, tvPrescriptionStatus;
            Button btnSelectMedicine;

            ViewHolder(View itemView) {
                super(itemView);
                tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
                tvMedicineDosage = itemView.findViewById(R.id.tvMedicineDosage);
                tvMedicinePrice = itemView.findViewById(R.id.tvMedicinePrice);
                tvMedicineDesc = itemView.findViewById(R.id.tvMedicineDesc);
                tvStockBadge = itemView.findViewById(R.id.tvStockBadge);
                tvPrescriptionStatus = itemView.findViewById(R.id.tvPrescriptionStatus);
                btnSelectMedicine = itemView.findViewById(R.id.btnSelectMedicine);
            }
        }
    }
}
