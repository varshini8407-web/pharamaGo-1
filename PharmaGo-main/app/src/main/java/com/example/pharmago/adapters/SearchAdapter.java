package com.example.pharmago.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SearchItem item);
    }

    private List<SearchItem> fullList;
    private List<SearchItem> itemList;
    private OnItemClickListener listener;
    private String currentQuery = "";
    private String currentTypeFilter = "ALL";

    public SearchAdapter(List<SearchItem> items, OnItemClickListener listener) {
        this.fullList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.itemList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (itemList == null || position < 0 || position >= itemList.size()) return;
        SearchItem item = itemList.get(position);
        if (item == null) return;

        boolean isHospital = "HOSPITAL".equalsIgnoreCase(item.getItemType());
        boolean isPharmacy = "PHARMACY".equalsIgnoreCase(item.getItemType());

        // Primary name: hospital name / pharmacy name / medicine name
        if (holder.tvMedicineName != null) {
            String prefix = isHospital ? "🏥 " : isPharmacy ? "🏪 " : "";
            holder.tvMedicineName.setText(prefix + item.getMedicineName());
        }
        // Dosage / subtitle
        if (holder.tvMedicineDosage != null) {
            if (isHospital || isPharmacy) {
                holder.tvMedicineDosage.setText(item.getDosage() != null && !item.getDosage().isEmpty()
                        ? item.getDosage() : (isHospital ? "Hospital Dispensary" : "Retail Pharmacy"));
            } else {
                holder.tvMedicineDosage.setText(item.getDosage() != null && !item.getDosage().isEmpty()
                        ? item.getDosage() : "Standard Dosage");
            }
        }
        if (holder.tvMedicinePrice != null) {
            holder.tvMedicinePrice.setText(item.getPrice() != null ? item.getPrice() : "₹45.00");
        }
        if (holder.tvMedicineDesc != null) {
            if (isHospital || isPharmacy) {
                holder.tvMedicineDesc.setText(item.getDescription() != null && !item.getDescription().isEmpty()
                        ? item.getDescription() : "Verified Healthcare Partner");
            } else {
                holder.tvMedicineDesc.setText("🏪 " + item.getPharmacyName()
                        + (item.getDescription() != null && !item.getDescription().isEmpty()
                        ? " • " + item.getDescription() : ""));
            }
        }
        if (holder.tvMedicineCategory != null) {
            holder.tvMedicineCategory.setText("Category: " + (item.getCategory() != null ? item.getCategory() : "General"));
        }

        // Requirement Part 1.3: Stock Badge
        if (holder.tvStockBadge != null) {
            if (isHospital || isPharmacy) {
                holder.tvStockBadge.setText("Open");
                holder.tvStockBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_mint));
                holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.secondary_container));
            } else if (item.isInStock()) {
                holder.tvStockBadge.setText("In Stock");
                holder.tvStockBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_mint));
                holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.secondary_container));
            } else {
                holder.tvStockBadge.setText("Out of Stock");
                holder.tvStockBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.danger_alert));
                holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.danger_container));
            }
        }

        // Prescription Badge
        if (holder.tvPrescriptionStatus != null) {
            if (isHospital) {
                holder.tvPrescriptionStatus.setText("Hospital");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_clinical_blue));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.primary_container));
            } else if (isPharmacy) {
                holder.tvPrescriptionStatus.setText("Pharmacy");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_mint));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.secondary_container));
            } else if (item.isRequiresPrescription()) {
                holder.tvPrescriptionStatus.setText("Prescription Required");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_warning_container));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.warning_container));
            } else {
                holder.tvPrescriptionStatus.setText("OTC");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.neutral_muted));
                holder.tvPrescriptionStatus.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.background_surface));
            }
        }

        // Action button label
        if (holder.btnSelectMedicine != null) {
            if (isHospital || isPharmacy) {
                holder.btnSelectMedicine.setText("Browse Store →");
            } else {
                holder.btnSelectMedicine.setText("+ Add to Cart");
            }
            holder.btnSelectMedicine.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query : "";
        applyFilterInternal();
    }

    public void filterByType(String type) {
        this.currentTypeFilter = type != null ? type : "ALL";
        applyFilterInternal();
    }

    private void applyFilterInternal() {
        if (itemList == null) {
            itemList = new ArrayList<>();
        } else {
            itemList.clear();
        }

        if (fullList == null) {
            fullList = new ArrayList<>();
        }

        String q = currentQuery.toLowerCase().trim();
        for (SearchItem item : fullList) {
            if (item == null) continue;

            // Check type filter: "ALL", "MEDICINE", "PHARMACY", "HOSPITAL"
            boolean matchType = true;
            if ("MEDICINE".equalsIgnoreCase(currentTypeFilter)) {
                matchType = "MEDICINE".equalsIgnoreCase(item.getItemType());
            } else if ("PHARMACY".equalsIgnoreCase(currentTypeFilter)) {
                matchType = "PHARMACY".equalsIgnoreCase(item.getItemType()) || (item.getCategory() != null && item.getCategory().toLowerCase().contains("pharmacy"));
            } else if ("HOSPITAL".equalsIgnoreCase(currentTypeFilter)) {
                matchType = "HOSPITAL".equalsIgnoreCase(item.getItemType()) || (item.getCategory() != null && item.getCategory().toLowerCase().contains("hospital"));
            }

            if (!matchType) continue;

            if (q.isEmpty()) {
                itemList.add(item);
            } else {
                boolean matchName = item.getMedicineName() != null && item.getMedicineName().toLowerCase().contains(q);
                boolean matchPharmacy = item.getPharmacyName() != null && item.getPharmacyName().toLowerCase().contains(q);
                boolean matchCategory = item.getCategory() != null && item.getCategory().toLowerCase().contains(q);
                boolean matchDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(q);
                if (matchName || matchPharmacy || matchCategory || matchDesc) {
                    itemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<SearchItem> newItems) {
        this.fullList = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        applyFilterInternal();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvMedicineDosage, tvMedicinePrice, tvMedicineDesc, tvMedicineCategory, tvStockBadge, tvPrescriptionStatus;
        Button btnSelectMedicine;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineDosage = itemView.findViewById(R.id.tvMedicineDosage);
            tvMedicinePrice = itemView.findViewById(R.id.tvMedicinePrice);
            tvMedicineDesc = itemView.findViewById(R.id.tvMedicineDesc);
            tvMedicineCategory = itemView.findViewById(R.id.tvMedicineCategory);
            tvStockBadge = itemView.findViewById(R.id.tvStockBadge);
            tvPrescriptionStatus = itemView.findViewById(R.id.tvPrescriptionStatus);
            btnSelectMedicine = itemView.findViewById(R.id.btnSelectMedicine);
        }
    }
}
