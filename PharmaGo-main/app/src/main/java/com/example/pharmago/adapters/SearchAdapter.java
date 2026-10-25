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

        if (holder.tvMedicineName != null) {
            holder.tvMedicineName.setText(item.getMedicineName() + " — " + item.getPrice());
        }
        if (holder.tvMedicineDesc != null) {
            holder.tvMedicineDesc.setText("🏪 " + item.getPharmacyName() + "\n" + item.getDescription());
        }
        if (holder.tvMedicineCategory != null) {
            holder.tvMedicineCategory.setText("Category: " + item.getCategory());
        }

        if (holder.tvPrescriptionStatus != null) {
            if (item.isRequiresPrescription()) {
                holder.tvPrescriptionStatus.setText("⚠️ Prescription Required");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.danger_alert));
            } else {
                holder.tvPrescriptionStatus.setText("✅ In Stock • OTC");
                holder.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
            }
        }

        if (holder.btnSelectMedicine != null) {
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
        if (itemList == null) {
            itemList = new ArrayList<>();
        } else {
            itemList.clear();
        }

        if (fullList == null) {
            fullList = new ArrayList<>();
        }

        if (query == null || query.trim().isEmpty()) {
            itemList.addAll(fullList);
        } else {
            String q = query.toLowerCase().trim();
            for (SearchItem item : fullList) {
                if (item != null) {
                    boolean matchName = item.getMedicineName() != null && item.getMedicineName().toLowerCase().contains(q);
                    boolean matchPharmacy = item.getPharmacyName() != null && item.getPharmacyName().toLowerCase().contains(q);
                    boolean matchCategory = item.getCategory() != null && item.getCategory().toLowerCase().contains(q);
                    boolean matchDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(q);
                    if (matchName || matchPharmacy || matchCategory || matchDesc) {
                        itemList.add(item);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<SearchItem> newItems) {
        this.fullList = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        this.itemList = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvMedicineDesc, tvMedicineCategory, tvPrescriptionStatus;
        Button btnSelectMedicine;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineDesc = itemView.findViewById(R.id.tvMedicineDesc);
            tvMedicineCategory = itemView.findViewById(R.id.tvMedicineCategory);
            tvPrescriptionStatus = itemView.findViewById(R.id.tvPrescriptionStatus);
            btnSelectMedicine = itemView.findViewById(R.id.btnSelectMedicine);
        }
    }
}
