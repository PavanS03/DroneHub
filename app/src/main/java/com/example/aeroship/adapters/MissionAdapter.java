package com.example.aeroship.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    private Context context;
    private List<Booking> list;
    private OnMissionClickListener listener;

    public interface OnMissionClickListener {
        void onStart(Booking booking);
        void onEnd(Booking booking);
        void onShowReceipt(Booking booking);
    }

    public MissionAdapter(Context context, List<Booking> list, OnMissionClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_mission_operator, parent, false);
        return new MissionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Booking booking = list.get(position);
        holder.tvOrderId.setText("Order #" + (booking.getBookingId().length() > 8 ? booking.getBookingId().substring(0, 8) : booking.getBookingId()));
        holder.tvUserName.setText("Customer: " + booking.getUserName());
        holder.tvDroneId.setText("Drone ID: " + (booking.getDroneId() != null ? booking.getDroneId() : "Not Assigned"));
        holder.tvSector.setText("Sector: " + booking.getSector());
        holder.tvTime.setText("Time: " + booking.getStartTime() + " - " + booking.getEndTime());
        holder.tvStatus.setText(booking.getStatus());

        String status = booking.getStatus();
        if (status.equalsIgnoreCase("Assigned") || status.equalsIgnoreCase("Approved")) {
            holder.btnStart.setVisibility(View.VISIBLE);
            holder.btnEnd.setVisibility(View.GONE);
            holder.btnReceipt.setVisibility(View.GONE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
        } else if (status.equalsIgnoreCase("In Progress")) {
            holder.btnStart.setVisibility(View.GONE);
            holder.btnEnd.setVisibility(View.VISIBLE);
            holder.btnReceipt.setVisibility(View.GONE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_busy);
        } else if (status.equalsIgnoreCase("Completed")) {
            holder.btnStart.setVisibility(View.GONE);
            holder.btnEnd.setVisibility(View.GONE);
            holder.btnReceipt.setVisibility(View.VISIBLE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
        }

        holder.btnStart.setOnClickListener(v -> listener.onStart(booking));
        holder.btnEnd.setOnClickListener(v -> listener.onEnd(booking));
        holder.btnReceipt.setOnClickListener(v -> listener.onShowReceipt(booking));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserName, tvDroneId, tvSector, tvTime, tvStatus;
        MaterialButton btnStart, btnEnd, btnReceipt;

        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvDroneId = itemView.findViewById(R.id.tvDroneId);
            tvSector = itemView.findViewById(R.id.tvSector);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnEnd = itemView.findViewById(R.id.btnEnd);
            btnReceipt = itemView.findViewById(R.id.btnReceipt);
        }
    }
}