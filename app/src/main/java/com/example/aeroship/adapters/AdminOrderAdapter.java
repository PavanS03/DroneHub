package com.example.aeroship.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.activities.AssignDroneActivity;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.List;

public class AdminOrderAdapter
        extends RecyclerView.Adapter<AdminOrderAdapter.AdminViewHolder> {

    private final List<Booking> bookingList;

    public AdminOrderAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);

        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AdminViewHolder holder,
            int position) {

        Booking booking = bookingList.get(position);
        if (booking == null) return;

        String userId = booking.getUserId();
        String sector = safe(booking.getSector());
        String level = safe(booking.getLevel());
        String date = safe(booking.getDate());

        double amount = booking.getTotalAmount();
        double advance = 500.0;
        final double finalRemaining = Math.max(0, amount - advance);

        boolean isPaid = Boolean.TRUE.equals(booking.getAdvancePaid());
        String paymentStatus = isPaid ? "Advance Paid ✅" : "Pending ❌";

        if (userId != null && !userId.isEmpty()) {

            FirebaseDatabase.getInstance()
                    .getReference("USERS")
                    .child(userId)
                    .child("name")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        String userName = snapshot.getValue(String.class);
                        if (userName == null) userName = "Unknown";

                        holder.tvOrderInfo.setText(
                                "User: " + userName +
                                        "\nSector: " + sector +
                                        "\nLevel: " + level +
                                        "\nDate: " + date +
                                        "\nTotal: ₹" + amount +
                                        "\nAdvance: ₹" + advance +
                                        "\nRemaining: ₹" + finalRemaining +
                                        "\nPayment: " + paymentStatus
                        );
                    });

        } else {
            holder.tvOrderInfo.setText("User: Unknown");
        }

        String status = safe(booking.getStatus());
        holder.tvStatus.setText(status.toUpperCase());

        updateStatusUI(holder, status, booking);

        holder.btnApprove.setOnClickListener(v -> {
            updateStatus(booking, "Approved");
            animateButton(v);
        });

        holder.btnReject.setOnClickListener(v -> {
            updateStatusWithInventory(booking, "Rejected");
            animateButton(v);
        });

        holder.btnAssign.setOnClickListener(v -> {

            if (!Boolean.TRUE.equals(booking.getAdvancePaid())) {
                Toast.makeText(v.getContext(),
                        "Payment not done",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(v.getContext(), AssignDroneActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            v.getContext().startActivity(intent);
        });

        holder.btnReassign.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AssignDroneActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            v.getContext().startActivity(intent);
        });

        holder.btnConfirm.setOnClickListener(v -> {
            updateStatusWithInventory(booking, "Completed");

            FirebaseDatabase.getInstance()
                    .getReference("ORDERS")
                    .child(booking.getBookingId())
                    .child("notification")
                    .setValue("Work Completed. Please pay remaining");

            Toast.makeText(v.getContext(),
                    "Marked Completed",
                    Toast.LENGTH_SHORT).show();
        });

        String operatorId = booking.getAssignedOperatorId();
        String operatorName = booking.getAssignedOperatorName();

        if (operatorName != null && !operatorName.isEmpty()) {
            holder.tvAssignedOperator.setText("Operator: " + operatorName);
        } else {
            holder.tvAssignedOperator.setText("Operator: Not Assigned");
        }

        if (operatorId != null && !operatorId.isEmpty()) {

            FirebaseDatabase.getInstance()
                    .getReference("USERS")
                    .child(operatorId)
                    .child("status")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        String opStatus = snapshot.getValue(String.class);

                        if ("busy".equalsIgnoreCase(opStatus)) {
                            holder.tvOperatorStatus.setText("Busy");
                            holder.tvOperatorStatus.setBackgroundResource(R.drawable.bg_status_busy);
                        } else {
                            holder.tvOperatorStatus.setText("Available");
                            holder.tvOperatorStatus.setBackgroundResource(R.drawable.bg_status_available);
                        }
                    });

        } else {
            holder.tvOperatorStatus.setText("Status: --");
        }
    }

    private void updateStatusUI(AdminViewHolder holder, String status, Booking booking) {

        String opId = booking.getAssignedOperatorId();

        switch (status.toLowerCase()) {

            case "approved":
                holder.tvStatus.setTextColor(Color.parseColor("#00C853"));
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);

                if ((opId == null || opId.isEmpty())
                        && Boolean.TRUE.equals(booking.getAdvancePaid())) {
                    holder.btnAssign.setVisibility(View.VISIBLE);
                }
                break;

            case "assigned":
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                holder.btnReassign.setVisibility(View.VISIBLE);
                break;

            case "completed":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;

            case "rejected":
                holder.tvStatus.setTextColor(Color.parseColor("#D50000"));
                break;

            default:
                holder.tvStatus.setTextColor(Color.parseColor("#FFD600"));
                break;
        }
    }

    private void updateStatus(Booking booking, String status) {
        if (booking.getBookingId() == null) return;
        
        FirebaseDatabase.getInstance()
                .getReference("ORDERS")
                .child(booking.getBookingId())
                .child("status")
                .setValue(status);
    }

    private void updateStatusWithInventory(Booking booking, String status) {
        if (booking.getBookingId() == null) return;

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("ORDERS")
                .child(booking.getBookingId());

        orderRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Booking b = currentData.getValue(Booking.class);
                if (b == null) return Transaction.success(currentData);

                Boolean released = currentData.child("inventoryReleased").getValue(Boolean.class);
                if (Boolean.TRUE.equals(released)) {
                    currentData.child("status").setValue(status);
                    return Transaction.success(currentData);
                }

                currentData.child("inventoryReleased").setValue(true);
                currentData.child("status").setValue(status);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed && currentData != null) {
                    releaseInventory(booking.getSector(), booking.getDroneCount());
                }
            }
        });
    }

    private void releaseInventory(String sector, int count) {
        if (sector == null || sector.isEmpty()) return;

        DatabaseReference sectorRef = FirebaseDatabase.getInstance()
                .getReference("SECTORS")
                .child(sector);

        sectorRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long available = currentData.child("droneCount").getValue(Long.class);
                Long max = currentData.child("maxDrones").getValue(Long.class);

                if (available != null) {
                    long newCount = available + count;
                    if (max != null && newCount > max) newCount = max;
                    currentData.child("droneCount").setValue(newCount);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
        });
    }

    private void animateButton(View view) {

        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .setInterpolator(new DecelerateInterpolator())
                                .start())
                .start();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderInfo, tvStatus;
        TextView tvAssignedOperator, tvOperatorStatus;

        MaterialButton btnApprove, btnReject, btnAssign, btnReassign, btnConfirm;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOrderInfo = itemView.findViewById(R.id.tvOrderInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAssignedOperator = itemView.findViewById(R.id.tvAssignedOperator);
            tvOperatorStatus = itemView.findViewById(R.id.tvOperatorStatus);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAssign = itemView.findViewById(R.id.btnAssignOperator);
            btnReassign = itemView.findViewById(R.id.btnReassignOperator);
            btnConfirm = itemView.findViewById(R.id.btnConfirmCompletion);
        }
    }
}