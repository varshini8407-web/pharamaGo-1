package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.NotificationModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    private List<NotificationModel> list;
    private OnNotificationClickListener listener;

    public NotificationAdapter(List<NotificationModel> list, OnNotificationClickListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        NotificationModel item = list.get(position);
        if (item == null) return;

        Context context = holder.itemView.getContext();

        holder.tvNotificationTitle.setText(item.getTitle());
        holder.tvNotificationBody.setText(item.getMessage());
        holder.tvNotificationTime.setText(item.getTime());

        // Unread dot indicator
        holder.viewUnreadDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

        // Icon styling based on notification type
        if ("ORDER".equalsIgnoreCase(item.getType())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_delivery);
            holder.ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            holder.tvNotificationAction.setText("Track Order ›");
        } else if ("PRESCRIPTION".equalsIgnoreCase(item.getType())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_nav_prescription);
            holder.ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondary_mint));
            holder.tvNotificationAction.setText("View Rx ›");
        } else if ("REMINDER".equalsIgnoreCase(item.getType())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_pill);
            holder.ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning_amber));
            holder.tvNotificationAction.setText("Take Medicine ›");
        } else {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_bell);
            holder.ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_clinical_blue));
            holder.tvNotificationAction.setText("View Details ›");
        }

        holder.itemView.setOnClickListener(v -> {
            item.setRead(true);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void clearAll() {
        if (list != null) {
            list.clear();
            notifyDataSetChanged();
        }
    }

    public void updateList(List<NotificationModel> newList) {
        this.list = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNotificationIcon;
        ImageView ivNotificationIcon;
        TextView tvNotificationTitle, tvNotificationBody, tvNotificationTime, tvNotificationAction;
        View viewUnreadDot;

        ViewHolder(View itemView) {
            super(itemView);
            cardNotificationIcon = itemView.findViewById(R.id.cardNotificationIcon);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationBody = itemView.findViewById(R.id.tvNotificationBody);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            tvNotificationAction = itemView.findViewById(R.id.tvNotificationAction);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
