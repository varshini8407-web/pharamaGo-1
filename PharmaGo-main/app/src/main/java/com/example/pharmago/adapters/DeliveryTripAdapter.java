package com.example.pharmago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmago.R;
import com.example.pharmago.models.DeliveryTripModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryTripAdapter extends RecyclerView.Adapter<DeliveryTripAdapter.TripViewHolder> {

    private final Context context;
    private final List<DeliveryTripModel> trips;

    public DeliveryTripAdapter(Context context, List<DeliveryTripModel> trips) {
        this.context = context;
        this.trips = trips != null ? trips : new ArrayList<>();
    }

    public void updateData(List<DeliveryTripModel> newTrips) {
        this.trips.clear();
        if (newTrips != null) {
            this.trips.addAll(newTrips);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        DeliveryTripModel trip = trips.get(position);

        holder.tvTripOrderId.setText(String.format(Locale.getDefault(), "Order #%d", trip.getOrderId()));
        holder.tvTripTimestamp.setText(trip.getCompletedAt() != null ? trip.getCompletedAt() : "Completed");
        holder.tvTripDistance.setText(String.format(Locale.getDefault(), "📍 %.1f km delivered", trip.getDistanceKm()));
        holder.tvTripTotalEarnings.setText(String.format(Locale.getDefault(), "₹%.2f", trip.getTotalEarning()));
        holder.tvTripPayoutAndTip.setText(String.format(Locale.getDefault(), "Base ₹%.0f • Tip ₹%.0f", trip.getPayout(), trip.getTip()));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripOrderId;
        TextView tvTripTimestamp;
        TextView tvTripDistance;
        TextView tvTripTotalEarnings;
        TextView tvTripPayoutAndTip;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripOrderId = itemView.findViewById(R.id.tvTripOrderId);
            tvTripTimestamp = itemView.findViewById(R.id.tvTripTimestamp);
            tvTripDistance = itemView.findViewById(R.id.tvTripDistance);
            tvTripTotalEarnings = itemView.findViewById(R.id.tvTripTotalEarnings);
            tvTripPayoutAndTip = itemView.findViewById(R.id.tvTripPayoutAndTip);
        }
    }
}
