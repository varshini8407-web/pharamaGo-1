package com.example.pharmago.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.PrescriptionMedicineItem;

import java.util.List;
import java.util.Locale;

public class PrescriptionMedicineAdapter extends RecyclerView.Adapter<PrescriptionMedicineAdapter.MedicineViewHolder> {

    public interface OnQuantityChangedListener {
        void onQuantityChanged();
    }

    private final Context context;
    private final List<PrescriptionMedicineItem> itemList;
    private final OnQuantityChangedListener listener;

    public PrescriptionMedicineAdapter(Context context, List<PrescriptionMedicineItem> itemList, OnQuantityChangedListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription_medicine_row, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        PrescriptionMedicineItem item = itemList.get(position);

        holder.tvMedicineName.setText(item.getMedicineName());
        holder.tvMedicineDosage.setText(item.getDosage());

        // Requirement 3: Real-time inventory stock badge
        if (item.getAvailableStock() > 0) {
            holder.tvMedicineStockBadge.setText(String.format(Locale.getDefault(), "● In Stock: %d units", item.getAvailableStock()));
            holder.tvMedicineStockBadge.setTextColor(ContextCompat.getColor(context, R.color.secondary_mint));
            holder.tvMedicineStockBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_container)));
        } else {
            holder.tvMedicineStockBadge.setText("✕ Out of Stock");
            holder.tvMedicineStockBadge.setTextColor(ContextCompat.getColor(context, R.color.danger_alert));
            holder.tvMedicineStockBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.danger_container)));
        }

        holder.tvUnitPrice.setText(String.format(Locale.getDefault(), "₹%.2f / unit", item.getUnitPrice()));
        holder.tvRequestedQty.setText(String.valueOf(item.getRequestedQty()));
        holder.tvItemSubtotal.setText(String.format(Locale.getDefault(), "Subtotal: ₹%.2f", item.getSubtotal()));

        // Interactive Quantity Adjustment
        holder.btnMinusQty.setOnClickListener(v -> {
            if (item.getRequestedQty() > 0) {
                item.setRequestedQty(item.getRequestedQty() - 1);
                holder.tvRequestedQty.setText(String.valueOf(item.getRequestedQty()));
                holder.tvItemSubtotal.setText(String.format(Locale.getDefault(), "Subtotal: ₹%.2f", item.getSubtotal()));
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            } else {
                Toast.makeText(context, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnPlusQty.setOnClickListener(v -> {
            if (item.getRequestedQty() < item.getAvailableStock()) {
                item.setRequestedQty(item.getRequestedQty() + 1);
                holder.tvRequestedQty.setText(String.valueOf(item.getRequestedQty()));
                holder.tvItemSubtotal.setText(String.format(Locale.getDefault(), "Subtotal: ₹%.2f", item.getSubtotal()));
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            } else {
                Toast.makeText(context, "Cannot exceed available inventory stock (" + item.getAvailableStock() + " units)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvMedicineDosage, tvMedicineStockBadge;
        TextView tvUnitPrice, tvItemSubtotal, tvRequestedQty;
        View btnMinusQty, btnPlusQty;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineDosage = itemView.findViewById(R.id.tvMedicineDosage);
            tvMedicineStockBadge = itemView.findViewById(R.id.tvMedicineStockBadge);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvItemSubtotal = itemView.findViewById(R.id.tvItemSubtotal);
            tvRequestedQty = itemView.findViewById(R.id.tvRequestedQty);
            btnMinusQty = itemView.findViewById(R.id.btnMinusQty);
            btnPlusQty = itemView.findViewById(R.id.btnPlusQty);
        }
    }
}
