package com.example.aeroship.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.activities.BookingDetailsActivity;
import com.example.aeroship.activities.UserFinalPaymentActivity;
import com.example.aeroship.activities.RatingActivity;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final List<Booking> fullList;

    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
        this.fullList = new java.util.ArrayList<>(bookingList);
    }

    public void updateList(List<Booking> newList) {
        bookingList.clear();
        bookingList.addAll(newList);
        fullList.clear();
        fullList.addAll(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        bookingList.clear();
        if (query.isEmpty()) {
            bookingList.addAll(fullList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Booking booking : fullList) {
                if (booking.getSector().toLowerCase().contains(filterPattern) ||
                    booking.getLevel().toLowerCase().contains(filterPattern)) {
                    bookingList.add(booking);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Booking booking = bookingList.get(position);
        if (booking == null) return;

        holder.tvSector.setText(safe(booking.getSector()));

        int total = booking.getTotalAmount();
        int advance = booking.getAdvanceAmount() > 0 ? booking.getAdvanceAmount() : 500;
        int remaining = booking.getRemainingAmount() > 0
                ? booking.getRemainingAmount()
                : total - advance;

        holder.tvDetails.setText(
                "Level: " + safe(booking.getLevel()) +
                        "\nDate: " + safe(booking.getDate()) +
                        "\nLocation: " + safe(booking.getLocation()) +
                        "\nDrones: " + booking.getDroneCount()
        );

        holder.tvAmount.setText("₹ " + total);

        String status = booking.getStatus() != null ? booking.getStatus() : "Pending";
        holder.tvStatus.setText(status);

        String paymentStatus = booking.getPaymentStatus();

        if (paymentStatus == null || paymentStatus.isEmpty()) {
            paymentStatus = "Pending";
        }

        holder.tvPaymentStatus.setText(paymentStatus);

        if ("Fully Paid".equalsIgnoreCase(paymentStatus)) {
            holder.tvPaymentStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else if ("Partial".equalsIgnoreCase(paymentStatus)) {
            holder.tvPaymentStatus.setBackgroundColor(Color.parseColor("#FF9800"));
        } else {
            holder.tvPaymentStatus.setBackgroundColor(Color.parseColor("#F44336"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(booking);
            }
        });

        if ("Approved".equals(status)) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else if ("Rejected".equals(status)) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
        } else if ("Completed".equals(status)) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#2196F3"));
        } else {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
        }

        holder.itemView.setTranslationY(100f);
        holder.itemView.setAlpha(0f);

        holder.itemView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        holder.cardRoot.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        });

        holder.btnTrack.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookingDetailsActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            v.getContext().startActivity(intent);
        });

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("ORDERS")
                .child(booking.getBookingId());

        Boolean advancePaid = booking.getAdvancePaid();

        if ("Approved".equals(status) && (advancePaid == null || !advancePaid)) {

            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setText("Pay ₹" + advance);

            holder.btnPay.setOnClickListener(v -> {

                ref.child("paymentStatus").setValue("Partial");
                ref.child("advancePaid").setValue(true);

                Toast.makeText(v.getContext(),
                        "Advance Paid ✅",
                        Toast.LENGTH_SHORT).show();
            });
        }

        else if ("Completed".equals(status)
                && !"Fully Paid".equalsIgnoreCase(paymentStatus)) {

            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setText("Pay ₹" + remaining);

            holder.btnPay.setOnClickListener(v -> {

                Intent intent = new Intent(v.getContext(),
                        UserFinalPaymentActivity.class);

                intent.putExtra("bookingId", booking.getBookingId());
                intent.putExtra("amount", remaining);

                v.getContext().startActivity(intent);
            });
        }

        else if ("Completed".equals(status)
                && "Fully Paid".equalsIgnoreCase(paymentStatus)) {

            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setText("Rate ⭐");

            holder.btnPay.setOnClickListener(v -> {

                Intent intent = new Intent(v.getContext(),
                        RatingActivity.class);

                intent.putExtra("bookingId", booking.getBookingId());

                v.getContext().startActivity(intent);
            });
        }

        else if ("Approved".equals(status) && Boolean.TRUE.equals(advancePaid)) {

            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setText("Advance Paid");
            holder.btnPay.setEnabled(false);
        }

        else {
            holder.btnPay.setVisibility(View.GONE);
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSector, tvDetails, tvAmount, tvStatus, tvPaymentStatus;
        MaterialButton btnTrack, btnPay;
        MaterialCardView cardRoot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSector = itemView.findViewById(R.id.tvSector);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);

            btnTrack = itemView.findViewById(R.id.btnTrack);
            btnPay = itemView.findViewById(R.id.btnPay);
            cardRoot = itemView.findViewById(R.id.cardRoot);
        }
    }
}