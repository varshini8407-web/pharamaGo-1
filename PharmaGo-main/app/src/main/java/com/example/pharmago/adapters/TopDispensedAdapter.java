package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopDispensedAdapter extends RecyclerView.Adapter<TopDispensedAdapter.TopMedViewHolder> {

    private final Context context;
    private List<InventoryItem> list;

    public TopDispensedAdapter(Context context, List<InventoryItem> list) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<>();
    }

    public void updateData(List<InventoryItem> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopMedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_dispensed_medicine, parent, false);
        return new TopMedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopMedViewHolder holder, int position) {
        InventoryItem item = list.get(position);

        holder.tvRank.setText("#" + (position + 1));
        holder.tvName.setText(item.getMedicineName());
        holder.tvCategory.setText(item.getCategory() + " • " + item.getDosage());

        int dispensed = item.getDispensedCount();
        holder.tvUnits.setText(dispensed + " units");

        double revenue = dispensed * item.getUnitPrice();
        holder.tvRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", revenue));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TopMedViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvCategory, tvUnits, tvRevenue;

        public TopMedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvMedRank);
            tvName = itemView.findViewById(R.id.tvTopMedName);
            tvCategory = itemView.findViewById(R.id.tvTopMedCategory);
            tvUnits = itemView.findViewById(R.id.tvTopMedDispensedUnits);
            tvRevenue = itemView.findViewById(R.id.tvTopMedRevenue);
        }
    }
}
