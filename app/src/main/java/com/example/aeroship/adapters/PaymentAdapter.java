package com.example.aeroship.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private Context context;
    private List<Booking> paymentList;
    private OnPaymentActionListener listener;

    public interface OnPaymentActionListener {
        void onApprove(Booking booking);
        void onReject(Booking booking);
    }

    public PaymentAdapter(Context context, List<Booking> paymentList, OnPaymentActionListener listener) {
        this.context = context;
        this.paymentList = paymentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Booking booking = paymentList.get(position);
        holder.tvOrderId.setText("Order ID: #" + booking.getBookingId());
        
        if (booking.getTransactionId() != null && !booking.getTransactionId().isEmpty()) {
            holder.tvTxnId.setVisibility(View.VISIBLE);
            holder.tvTxnId.setText("Txn ID: " + booking.getTransactionId());
        } else {
            holder.tvTxnId.setVisibility(View.GONE);
        }

        if (booking.getScreenshotUrl() != null && !booking.getScreenshotUrl().isEmpty()) {
            holder.ivScreenshot.setVisibility(View.VISIBLE);
            Glide.with(context).load(booking.getScreenshotUrl()).into(holder.ivScreenshot);
        } else {
            holder.ivScreenshot.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(booking));
        holder.btnReject.setOnClickListener(v -> listener.onReject(booking));
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTxnId;
        ImageView ivScreenshot;
        MaterialButton btnApprove, btnReject;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTxnId = itemView.findViewById(R.id.tvTxnId);
            ivScreenshot = itemView.findViewById(R.id.ivScreenshot);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}