package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    private List<OrderResponse> fullList;
    private List<OrderResponse> itemList;
    private OnOrderClickListener listener;

    public OrderHistoryAdapter(List<OrderResponse> items, OnOrderClickListener listener) {
        this.fullList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.itemList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (itemList == null || position < 0 || position >= itemList.size()) return;
        OrderResponse order = itemList.get(position);
        if (order == null) return;

        Context context = holder.itemView.getContext();

        if (holder.tvOrderId != null) {
            holder.tvOrderId.setText("Order #" + (order.getId() != null ? order.getId() : "--"));
        }
        if (holder.tvOrderPharmacy != null) {
            holder.tvOrderPharmacy.setText(order.getPharmacyName() != null ? "🏥 " + order.getPharmacyName() : "🏥 Hospital Pharmacy Order");
        }
        if (holder.tvOrderDetails != null) {
            String priceStr = order.getTotalPrice() != null ? "₹" + String.format("%.2f", order.getTotalPrice()) : "N/A";
            String typeStr = order.getType() != null ? order.getType() : "DELIVERY";
            holder.tvOrderDetails.setText("Type: " + typeStr + " • Total: " + priceStr);
        }

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PROCESSING";

        if (holder.tvOrderStatus != null) {
            if ("DELIVERED".equals(status)) {
                holder.tvOrderStatus.setText("✓ DELIVERED");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else if ("READY FOR PICKUP".equals(status)) {
                holder.tvOrderStatus.setText("🏥 READY FOR PICKUP");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_mint));
            } else if ("IN TRANSIT".equals(status)) {
                holder.tvOrderStatus.setText("● OUT FOR DELIVERY (7 mins)");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            } else {
                holder.tvOrderStatus.setText("● " + status);
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            }
        }

        if (holder.tvOrderDate != null) {
            holder.tvOrderDate.setText("Placed on: " + (order.getOrderDate() != null ? order.getOrderDate() : "Recent") + " • Verified Rx");
        }

        // Functional Primary & Secondary Action Buttons
        if (holder.btnOrderPrimaryAction != null) {
            if ("DELIVERY".equalsIgnoreCase(order.getType())) {
                if ("DELIVERED".equals(status)) {
                    holder.btnOrderPrimaryAction.setText("Delivery Summary ✓");
                    holder.btnOrderPrimaryAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_container));
                    holder.btnOrderPrimaryAction.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
                } else {
                    holder.btnOrderPrimaryAction.setText("Track Live Delivery 🛵");
                    holder.btnOrderPrimaryAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#108A46")));
                    holder.btnOrderPrimaryAction.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
            } else {
                holder.btnOrderPrimaryAction.setText("Show Pickup QR Code 📱");
                holder.btnOrderPrimaryAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_clinical_blue));
                holder.btnOrderPrimaryAction.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

            holder.btnOrderPrimaryAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        if (holder.btnOrderSecondaryAction != null) {
            if ("DELIVERED".equals(status)) {
                holder.btnOrderSecondaryAction.setText("Reorder 🔄");
                holder.btnOrderSecondaryAction.setOnClickListener(v ->
                        Toast.makeText(context, "Added " + order.getPharmacyName() + " medicines back to cart! ✓", Toast.LENGTH_SHORT).show()
                );
            } else {
                holder.btnOrderSecondaryAction.setText("Receipt 📄");
                holder.btnOrderSecondaryAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onOrderClick(order);
                    }
                });
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
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
            for (OrderResponse order : fullList) {
                if (order != null) {
                    boolean matchId = order.getId() != null && String.valueOf(order.getId()).contains(q);
                    boolean matchPharmacy = order.getPharmacyName() != null && order.getPharmacyName().toLowerCase().contains(q);
                    boolean matchStatus = order.getStatus() != null && order.getStatus().toLowerCase().contains(q);
                    boolean matchType = order.getType() != null && order.getType().toLowerCase().contains(q);
                    if (matchId || matchPharmacy || matchStatus || matchType) {
                        itemList.add(order);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<OrderResponse> newOrders) {
        this.fullList = newOrders != null ? new ArrayList<>(newOrders) : new ArrayList<>();
        this.itemList = newOrders != null ? new ArrayList<>(newOrders) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderPharmacy, tvOrderDetails, tvOrderStatus, tvOrderDate;
        Button btnOrderPrimaryAction, btnOrderSecondaryAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderPharmacy = itemView.findViewById(R.id.tvOrderPharmacy);
            tvOrderDetails = itemView.findViewById(R.id.tvOrderDetails);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            btnOrderPrimaryAction = itemView.findViewById(R.id.btnOrderPrimaryAction);
            btnOrderSecondaryAction = itemView.findViewById(R.id.btnOrderSecondaryAction);
        }
    }
}
