package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.InventoryItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnStockChangeListener {
        void onStockChanged(InventoryItem item, int newStock);
    }

    private final Context context;
    private List<InventoryItem> fullList;
    private List<InventoryItem> displayList;
    private final OnStockChangeListener listener;

    public InventoryAdapter(Context context, List<InventoryItem> list, OnStockChangeListener listener) {
        this.context = context;
        this.fullList = list != null ? list : new ArrayList<>();
        this.displayList = new ArrayList<>(this.fullList);
        this.listener = listener;
    }

    public void updateData(List<InventoryItem> newList) {
        this.fullList = newList != null ? newList : new ArrayList<>();
        this.displayList = new ArrayList<>(this.fullList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_medicine, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = displayList.get(position);

        holder.tvName.setText(item.getMedicineName());
        holder.tvDosage.setText(item.getDosage());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getUnitPrice()));
        holder.tvCategory.setText(item.getCategory());
        holder.tvBatchExpiry.setText("Batch: " + item.getBatchNo() + " • Exp: " + item.getExpiryDate());

        int stock = item.getAvailableStock();
        holder.tvCurrentStockQty.setText(String.valueOf(stock));

        // Color-code stock indicator pill
        if (stock > 20) {
            holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_container));
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.on_secondary_container));
            holder.tvStockBadge.setText("In Stock: " + stock + " units");
        } else if (stock > 10) {
            holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.warning_container));
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.on_warning_container));
            holder.tvStockBadge.setText("Low: " + stock + " units");
        } else {
            holder.tvStockBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.danger_container));
            holder.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.on_danger_container));
            holder.tvStockBadge.setText("Critical: " + stock + " units");
        }

        holder.btnMinus.setOnClickListener(v -> {
            int current = item.getAvailableStock();
            if (current > 0) {
                int newStock = current - 1;
                item.setAvailableStock(newStock);
                notifyItemChanged(position);
                if (listener != null) listener.onStockChanged(item, newStock);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            int newStock = item.getAvailableStock() + 1;
            item.setAvailableStock(newStock);
            notifyItemChanged(position);
            if (listener != null) listener.onStockChanged(item, newStock);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvPrice, tvCategory, tvBatchExpiry, tvStockBadge, tvCurrentStockQty;
        MaterialButton btnMinus, btnPlus;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvInvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvInvDosage);
            tvPrice = itemView.findViewById(R.id.tvInvUnitPrice);
            tvCategory = itemView.findViewById(R.id.tvInvCategoryBadge);
            tvBatchExpiry = itemView.findViewById(R.id.tvInvBatchExpiry);
            tvStockBadge = itemView.findViewById(R.id.tvInvStockBadge);
            tvCurrentStockQty = itemView.findViewById(R.id.tvInvCurrentStockQty);
            btnMinus = itemView.findViewById(R.id.btnInvStockMinus);
            btnPlus = itemView.findViewById(R.id.btnInvStockPlus);
        }
    }
}
