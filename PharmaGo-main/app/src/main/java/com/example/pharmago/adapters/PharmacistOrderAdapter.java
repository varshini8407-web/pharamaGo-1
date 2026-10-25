package com.example.pharmago.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.OrderModel;
import com.example.pharmago.models.OrderStatus;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PharmacistOrderAdapter extends RecyclerView.Adapter<PharmacistOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onReviewOrder(OrderModel order);
    }

    private final Context context;
    private List<OrderModel> fullList = new ArrayList<>();
    private List<OrderModel> displayedList = new ArrayList<>();
    private final OnOrderClickListener listener;
    private String currentFilter = "ALL";

    public PharmacistOrderAdapter(Context context, List<OrderModel> orders, OnOrderClickListener listener) {
        this.context = context;
        this.fullList = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
        this.displayedList = new ArrayList<>(this.fullList);
        this.listener = listener;
    }

    public void setOrders(List<OrderModel> newOrders) {
        this.fullList = newOrders != null ? new ArrayList<>(newOrders) : new ArrayList<>();
        filter(currentFilter);
    }

    public void updateData(List<OrderModel> newOrders) {
        setOrders(newOrders);
    }

    public void filterOrders(String filterType) {
        filter(filterType);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pharmacist_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = displayedList.get(position);

        holder.tvPharmacistOrderId.setText(String.format(Locale.getDefault(), "Order #%d", order.getOrderId()));
        holder.tvPharmacistOrderTimestamp.setText(order.getOrderTimestamp());
        holder.tvPharmacistPatientName.setText(order.getPatientName());
        holder.tvPharmacistPatientPhone.setText(order.getPatientPhone());

        // Requirement 2: Type Badge: Distinct blue pill badge for "TAKEAWAY" vs amber/orange pill badge for "DELIVERY"
        if ("TAKEAWAY".equalsIgnoreCase(order.getOrderType())) {
            holder.tvPharmacistOrderTypeBadge.setText("TAKEAWAY 🏥");
            holder.tvPharmacistOrderTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            holder.tvPharmacistOrderTypeBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_container)));
        } else {
            holder.tvPharmacistOrderTypeBadge.setText("DELIVERY 🛵");
            holder.tvPharmacistOrderTypeBadge.setTextColor(Color.parseColor("#D97706")); // amber text
            holder.tvPharmacistOrderTypeBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning_container)));
        }

        // Status Badge: "PENDING_VERIFICATION", "READY_FOR_PICKUP", "READY_FOR_DELIVERY"
        OrderStatus status = order.getStatus();
        if (status == OrderStatus.READY_FOR_PICKUP) {
            holder.tvPharmacistStatusBadge.setText("✓ READY FOR PICKUP");
            holder.tvPharmacistStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            holder.tvPharmacistStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_container)));
        } else if (status == OrderStatus.READY_FOR_DELIVERY) {
            holder.tvPharmacistStatusBadge.setText("● READY FOR DELIVERY");
            holder.tvPharmacistStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.secondary_mint));
            holder.tvPharmacistStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_container)));
        } else {
            holder.tvPharmacistStatusBadge.setText("● PENDING VERIFICATION");
            holder.tvPharmacistStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_amber));
            holder.tvPharmacistStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning_container)));
        }

        int medCount = order.getMedicineList() != null ? order.getMedicineList().size() : 0;
        holder.tvPharmacistOrderDetails.setText(String.format(Locale.getDefault(), "Rx Uploaded • %d Medicines • Total: ₹%.2f", medCount, order.getTotalAmount()));

        if ("TAKEAWAY".equalsIgnoreCase(order.getOrderType())) {
            holder.tvPharmacistOrderLocation.setText("📍 " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Hospital Pickup Counter"));
        } else {
            holder.tvPharmacistOrderLocation.setText("🏠 " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Home Delivery"));
        }

        // Security token badge if already approved
        if (order.getOtp() != null && !order.getOtp().isEmpty()) {
            holder.layoutSecurityTokenBadge.setVisibility(View.VISIBLE);
            holder.tvSecurityTokenInfo.setText("Patient Delivery OTP: " + order.getOtp() + " (Verified)");
            holder.ivSecurityTokenIcon.setImageResource(R.drawable.ic_check_circle);
        } else if (order.getQrCodeString() != null && !order.getQrCodeString().isEmpty()) {
            holder.layoutSecurityTokenBadge.setVisibility(View.VISIBLE);
            holder.tvSecurityTokenInfo.setText("Counter Pickup QR Token Generated ✓");
            holder.ivSecurityTokenIcon.setImageResource(R.drawable.ic_qr_code);
        } else {
            holder.layoutSecurityTokenBadge.setVisibility(View.GONE);
        }

        // Button state
        if (status == OrderStatus.PENDING_VERIFICATION) {
            holder.btnReviewOrder.setText("Review & Process 📋");
            holder.btnReviewOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_clinical_blue)));
        } else {
            holder.btnReviewOrder.setText("View Verified Invoice 📄");
            holder.btnReviewOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_mint)));
        }

        holder.btnReviewOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewOrder(order);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewOrder(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedList != null ? displayedList.size() : 0;
    }

    /**
     * Requirement 2: Filter feed by "All Orders", "Takeaway (QR)", "Delivery (OTP)"
     */
    public void filter(String filterType) {
        this.currentFilter = filterType != null ? filterType.toUpperCase() : "ALL";
        displayedList.clear();

        if ("ALL".equalsIgnoreCase(currentFilter)) {
            displayedList.addAll(fullList);
        } else if ("TAKEAWAY".equalsIgnoreCase(currentFilter)) {
            for (OrderModel o : fullList) {
                if ("TAKEAWAY".equalsIgnoreCase(o.getOrderType())) {
                    displayedList.add(o);
                }
            }
        } else if ("DELIVERY".equalsIgnoreCase(currentFilter)) {
            for (OrderModel o : fullList) {
                if ("DELIVERY".equalsIgnoreCase(o.getOrderType())) {
                    displayedList.add(o);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvPharmacistOrderId, tvPharmacistOrderTypeBadge, tvPharmacistStatusBadge;
        TextView tvPharmacistPatientName, tvPharmacistPatientPhone;
        TextView tvPharmacistOrderDetails, tvPharmacistOrderTimestamp, tvPharmacistOrderLocation;
        LinearLayout layoutSecurityTokenBadge;
        TextView tvSecurityTokenInfo;
        ImageView ivSecurityTokenIcon;
        MaterialButton btnReviewOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPharmacistOrderId = itemView.findViewById(R.id.tvPharmacistOrderId);
            tvPharmacistOrderTypeBadge = itemView.findViewById(R.id.tvPharmacistOrderTypeBadge);
            tvPharmacistStatusBadge = itemView.findViewById(R.id.tvPharmacistStatusBadge);
            tvPharmacistPatientName = itemView.findViewById(R.id.tvPharmacistPatientName);
            tvPharmacistPatientPhone = itemView.findViewById(R.id.tvPharmacistPatientPhone);
            tvPharmacistOrderDetails = itemView.findViewById(R.id.tvPharmacistOrderDetails);
            tvPharmacistOrderTimestamp = itemView.findViewById(R.id.tvPharmacistOrderTimestamp);
            tvPharmacistOrderLocation = itemView.findViewById(R.id.tvPharmacistOrderLocation);
            layoutSecurityTokenBadge = itemView.findViewById(R.id.layoutSecurityTokenBadge);
            tvSecurityTokenInfo = itemView.findViewById(R.id.tvSecurityTokenInfo);
            ivSecurityTokenIcon = itemView.findViewById(R.id.ivSecurityTokenIcon);
            btnReviewOrder = itemView.findViewById(R.id.btnReviewOrder);
        }
    }
}
