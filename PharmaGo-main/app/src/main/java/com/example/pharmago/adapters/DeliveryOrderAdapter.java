package com.example.pharmago.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.models.OrderStatus;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryOrderAdapter extends RecyclerView.Adapter<DeliveryOrderAdapter.DeliveryViewHolder> {

    public interface OnDeliveryOrderClickListener {
        void onAcceptOrder(DeliveryOrderModel order);
        void onOpenMapNavigation(DeliveryOrderModel order);
        void onVerifyOtp(DeliveryOrderModel order);
        void onCallCustomer(DeliveryOrderModel order);
    }

    private final Context context;
    private List<DeliveryOrderModel> fullList;
    private List<DeliveryOrderModel> displayList;
    private final OnDeliveryOrderClickListener listener;
    private String currentTab = "NEW";

    public DeliveryOrderAdapter(Context context, List<DeliveryOrderModel> list, OnDeliveryOrderClickListener listener) {
        this.context = context;
        this.fullList = list != null ? list : new ArrayList<>();
        this.displayList = new ArrayList<>(this.fullList);
        this.listener = listener;
    }

    public void updateData(List<DeliveryOrderModel> newList) {
        this.fullList = newList != null ? newList : new ArrayList<>();
        filterByTab(this.currentTab);
    }

    public void filterByTab(String tab) {
        this.currentTab = tab != null ? tab : "NEW";
        displayList.clear();

        for (DeliveryOrderModel order : fullList) {
            OrderStatus status = order.getOrderStatus();
            if ("NEW".equalsIgnoreCase(currentTab)) {
                if (status == OrderStatus.READY_FOR_DELIVERY) {
                    displayList.add(order);
                }
            } else if ("ACTIVE".equalsIgnoreCase(currentTab) || "IN_TRANSIT".equalsIgnoreCase(currentTab)) {
                if (status == OrderStatus.OUT_FOR_DELIVERY) {
                    displayList.add(order);
                }
            } else if ("HISTORY".equalsIgnoreCase(currentTab) || "COMPLETED".equalsIgnoreCase(currentTab)) {
                if (status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED) {
                    displayList.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery_order, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        DeliveryOrderModel order = displayList.get(position);

        holder.tvOrderId.setText(String.format(Locale.getDefault(), "Order #%d", order.getOrderId()));
        holder.tvTimestamp.setText(order.getOrderTimestamp());
        holder.tvCustomerName.setText(order.getPatientName());
        holder.tvCustomerPhone.setText(order.getPatientPhone());
        holder.tvPharmacyAddress.setText(order.getPharmacyAddress());
        holder.tvPatientAddress.setText(order.getDeliveryAddress());
        holder.tvMedSummary.setText("📦 " + order.getMedicineSummary());
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));

        // Payment status badge
        if (order.isCashOnDelivery()) {
            holder.tvPaymentBadge.setText(String.format(Locale.getDefault(), "💵 CASH ON DELIVERY: ₹%.2f", order.getTotalAmount()));
            holder.tvPaymentBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.warning_container));
            holder.tvPaymentBadge.setTextColor(ContextCompat.getColor(context, R.color.on_warning_container));
        } else {
            holder.tvPaymentBadge.setText("✓ PAID ONLINE (UPI)");
            holder.tvPaymentBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_container));
            holder.tvPaymentBadge.setTextColor(ContextCompat.getColor(context, R.color.on_secondary_container));
        }

        // Status badge & Contextual Buttons
        OrderStatus status = order.getOrderStatus();
        if (status == OrderStatus.READY_FOR_DELIVERY) {
            holder.tvStatusBadge.setText("READY FOR PICKUP");
            holder.tvStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_container));
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));

            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.layoutInTransitActions.setVisibility(View.GONE);
            holder.btnDeliveredReceipt.setVisibility(View.GONE);
        } else if (status == OrderStatus.OUT_FOR_DELIVERY) {
            holder.tvStatusBadge.setText("● IN TRANSIT");
            holder.tvStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.warning_container));
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_amber));

            holder.btnAccept.setVisibility(View.GONE);
            holder.layoutInTransitActions.setVisibility(View.VISIBLE);
            holder.btnDeliveredReceipt.setVisibility(View.GONE);
        } else {
            holder.tvStatusBadge.setText("✓ DELIVERED");
            holder.tvStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_container));
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.secondary_mint));

            holder.btnAccept.setVisibility(View.GONE);
            holder.layoutInTransitActions.setVisibility(View.GONE);
            holder.btnDeliveredReceipt.setVisibility(View.VISIBLE);
        }

        // Listeners
        holder.btnQuickCall.setOnClickListener(v -> {
            if (listener != null) listener.onCallCustomer(order);
        });

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAcceptOrder(order);
        });

        holder.btnOpenMapNav.setOnClickListener(v -> {
            if (listener != null) listener.onOpenMapNavigation(order);
        });

        holder.btnVerifyOtp.setOnClickListener(v -> {
            if (listener != null) listener.onVerifyOtp(order);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTimestamp, tvStatusBadge;
        TextView tvCustomerName, tvCustomerPhone;
        TextView tvPharmacyAddress, tvPatientAddress;
        TextView tvMedSummary, tvPaymentBadge, tvTotalAmount;
        View btnQuickCall;
        MaterialButton btnAccept, btnOpenMapNav, btnVerifyOtp, btnDeliveredReceipt;
        View layoutInTransitActions;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvDeliveryOrderId);
            tvTimestamp = itemView.findViewById(R.id.tvDeliveryTimestamp);
            tvStatusBadge = itemView.findViewById(R.id.tvDeliveryStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvDeliveryCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvDeliveryCustomerPhone);
            tvPharmacyAddress = itemView.findViewById(R.id.tvDeliveryPharmacyAddress);
            tvPatientAddress = itemView.findViewById(R.id.tvDeliveryPatientAddress);
            tvMedSummary = itemView.findViewById(R.id.tvDeliveryMedSummary);
            tvPaymentBadge = itemView.findViewById(R.id.tvDeliveryPaymentBadge);
            tvTotalAmount = itemView.findViewById(R.id.tvDeliveryTotalAmount);
            btnQuickCall = itemView.findViewById(R.id.btnQuickCallCustomer);
            btnAccept = itemView.findViewById(R.id.btnAcceptDelivery);
            btnOpenMapNav = itemView.findViewById(R.id.btnOpenMapNav);
            btnVerifyOtp = itemView.findViewById(R.id.btnVerifyOtpComplete);
            btnDeliveredReceipt = itemView.findViewById(R.id.btnViewDeliveredReceipt);
            layoutInTransitActions = itemView.findViewById(R.id.layoutInTransitActions);
        }
    }
}
