package com.example.aeroship.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.Payment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminPaymentAdapter
        extends RecyclerView.Adapter<AdminPaymentAdapter.ViewHolder> {

    private java.util.List<Payment> list;

    public AdminPaymentAdapter(java.util.List<Payment> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_payment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Payment payment = list.get(position);

        holder.tvUser.setText("User: " + payment.getUserName());
        holder.tvAmount.setText("₹ " + payment.getAmount());
        holder.tvStatus.setText("Status: " + payment.getStatus());

        String time = new SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault())
                .format(new Date(payment.getTimestamp()));

        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUser, tvAmount, tvStatus, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUser = itemView.findViewById(R.id.tvUser);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}