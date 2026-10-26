package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.OrderResponse;
import com.example.pharmago.utils.InvoiceReceiptBottomSheet;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    public interface OnOrderActionListener {
        void onOrderClick(OrderResponse order);
        void onViewPickupQR(OrderResponse order);
        void onViewInvoiceReceipt(OrderResponse order);
        void onTrackDelivery(OrderResponse order);
    }

    private List<OrderResponse> fullList;
    private List<OrderResponse> itemList;
    private OnOrderClickListener listener;
    private OnOrderActionListener actionListener;

    public OrderHistoryAdapter(List<OrderResponse> items, OnOrderClickListener listener) {
        this.fullList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.itemList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.listener = listener;
    }

    public OrderHistoryAdapter(List<OrderResponse> items, OnOrderActionListener actionListener) {
        this.fullList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.itemList = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.actionListener = actionListener;
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
        String type = order.getType() != null ? order.getType().toUpperCase() : "DELIVERY";

        if (holder.tvOrderStatus != null) {
            if ("DELIVERED".equals(status)) {
                holder.tvOrderStatus.setText("✓ DELIVERED");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else if (status.contains("PICKUP") || status.contains("READY")) {
                holder.tvOrderStatus.setText("🏥 READY FOR PICKUP");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_mint));
            } else if ("IN TRANSIT".equals(status) || status.contains("OUT_FOR_DELIVERY")) {
                holder.tvOrderStatus.setText("● OUT FOR DELIVERY");
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            } else {
                holder.tvOrderStatus.setText("● " + status);
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            }
        }

        if (holder.tvOrderDate != null) {
            holder.tvOrderDate.setText("Placed on: " + (order.getOrderDate() != null ? order.getOrderDate() : "Recent") + " • Verified Rx");
        }

        // Requirement Part 1.4: Delivery OTP Card prominent for Delivery orders
        boolean isDelivery = "DELIVERY".equalsIgnoreCase(type);
        if (holder.cardDeliveryOtp != null) {
            holder.cardDeliveryOtp.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
            if (isDelivery && holder.tvDeliveryOtpCode != null) {
                long orderNum = order.getId() != null ? order.getId() : 103L;
                int digit1 = (int) ((orderNum * 7 + 1) % 9) + 1;
                int digit2 = (int) ((orderNum * 3 + 2) % 9) + 1;
                int digit3 = (int) ((orderNum * 5 + 4) % 9) + 1;
                int digit4 = (int) ((orderNum * 2 + 5) % 9) + 1;
                int digit5 = (int) ((orderNum * 8 + 3) % 9) + 1;
                int digit6 = (int) ((orderNum * 4 + 7) % 9) + 1;
                holder.tvDeliveryOtpCode.setText(String.format("[ %d %d %d %d %d %d ]", digit1, digit2, digit3, digit4, digit5, digit6));
            }
        }

        // Requirement Part 1.4: Split Actions:
        // 1. "View Pickup QR" (Visible ONLY if order_type == 'TAKEAWAY' and status is READY_FOR_PICKUP)
        boolean isTakeawayReady = "TAKEAWAY".equalsIgnoreCase(type) && (status.contains("READY") || status.contains("PICKUP"));
        if (holder.btnViewPickupQR != null) {
            holder.btnViewPickupQR.setVisibility(isTakeawayReady ? View.VISIBLE : View.GONE);
            holder.btnViewPickupQR.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onViewPickupQR(order);
                } else if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        // Track Delivery Button: visible for ALL delivery orders so user can always access tracking
        if (holder.btnTrackDelivery != null) {
            holder.btnTrackDelivery.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
            holder.btnTrackDelivery.setText(status.contains("DELIVERED") ? "View Delivery Summary" : "Track Delivery 🛵");
            holder.btnTrackDelivery.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onTrackDelivery(order);
                } else if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        // 2. "View Invoice Receipt" (For all completed/approved orders)
        if (holder.btnViewInvoiceReceipt != null) {
            holder.btnViewInvoiceReceipt.setVisibility(View.VISIBLE);
            holder.btnViewInvoiceReceipt.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onViewInvoiceReceipt(order);
                } else {
                    if (context instanceof androidx.fragment.app.FragmentActivity) {
                        androidx.fragment.app.FragmentActivity act = (androidx.fragment.app.FragmentActivity) context;
                        double price = order.getTotalPrice() != null ? order.getTotalPrice() : 120.00;
                        InvoiceReceiptBottomSheet.show(act.getSupportFragmentManager(),
                                order.getId() != null ? order.getId() : 101L,
                                order.getPharmacyName() != null ? order.getPharmacyName() : "Apollo Central Pharmacy",
                                order.getType() != null ? order.getType() : "TAKEAWAY",
                                price,
                                "UPI (Instant Transfer)");
                    }
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onOrderClick(order);
            } else if (listener != null) {
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
        TextView tvOrderId, tvOrderPharmacy, tvOrderDetails, tvOrderStatus, tvOrderDate, tvDeliveryOtpCode;
        MaterialCardView cardDeliveryOtp;
        Button btnViewPickupQR, btnTrackDelivery, btnViewInvoiceReceipt;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderPharmacy = itemView.findViewById(R.id.tvOrderPharmacy);
            tvOrderDetails = itemView.findViewById(R.id.tvOrderDetails);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvDeliveryOtpCode = itemView.findViewById(R.id.tvDeliveryOtpCode);
            cardDeliveryOtp = itemView.findViewById(R.id.cardDeliveryOtp);
            btnViewPickupQR = itemView.findViewById(R.id.btnViewPickupQR);
            btnTrackDelivery = itemView.findViewById(R.id.btnTrackDelivery);
            btnViewInvoiceReceipt = itemView.findViewById(R.id.btnViewInvoiceReceipt);
        }
    }
}
