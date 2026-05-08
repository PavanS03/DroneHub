package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView tvSector, tvLevel, tvDate, tvLocation,
            tvDroneCount, tvAmount, tvStatus, tvOperatorInfo, tvDroneInfo;

    private MaterialButton btnPayNow;

    private TextView stepPending, stepApproved,
            stepAssigned, stepInProgress, stepCompleted;

    private DatabaseReference reference;
    private ValueEventListener bookingListener;
    private String bookingId;

    private View contentCard;

    private Booking currentBooking; // 🔥 keep reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        bookingId = getIntent().getStringExtra("bookingId");

        initViews();

        if (bookingId == null) {
            Toast.makeText(this, "Invalid Booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBookingDetails();
        animateEntry();
    }

    private void initViews() {

        contentCard = findViewById(R.id.contentCard);

        tvSector = findViewById(R.id.tvSector);
        tvLevel = findViewById(R.id.tvLevel);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvDroneCount = findViewById(R.id.tvDroneCount);
        tvAmount = findViewById(R.id.tvAmount);
        tvStatus = findViewById(R.id.tvStatus);

        tvOperatorInfo = findViewById(R.id.tvOperatorInfo);
        tvDroneInfo = findViewById(R.id.tvDroneInfo);

        btnPayNow = findViewById(R.id.btnPayNow);

        stepPending = findViewById(R.id.stepPending);
        stepApproved = findViewById(R.id.stepApproved);
        stepAssigned = findViewById(R.id.stepAssigned);
        stepInProgress = findViewById(R.id.stepInProgress);
        stepCompleted = findViewById(R.id.stepCompleted);
    }

    private void loadBookingDetails() {

        reference = FirebaseDatabase.getInstance()
                .getReference("ORDERS")
                .child(bookingId);

        bookingListener = reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Booking booking = snapshot.getValue(Booking.class);
                if (booking == null) return;

                currentBooking = booking;

                tvSector.setText("Sector: " + safe(booking.getSector()));
                tvLevel.setText("Level: " + safe(booking.getLevel()));
                tvDate.setText("Date: " + safe(booking.getDate()));
                tvLocation.setText("Location: " + safe(booking.getLocation()));
                tvDroneCount.setText("Drones: " + booking.getDroneCount());
                tvAmount.setText("₹ " + booking.getTotalAmount());

                // 🔥 DISPLAY OPERATOR & DRONE DETAILS
                if (booking.getAssignedOperatorName() != null && !booking.getAssignedOperatorName().isEmpty()) {
                    tvOperatorInfo.setVisibility(View.VISIBLE);
                    tvDroneInfo.setVisibility(View.VISIBLE);

                    String opText = "Operator: " + booking.getAssignedOperatorName();
                    if (booking.getAssignedOperatorPhone() != null && !booking.getAssignedOperatorPhone().isEmpty()) {
                        opText += " (" + booking.getAssignedOperatorPhone() + ")";
                    }
                    tvOperatorInfo.setText(opText);
                    
                    String droneText = "Drone ID: " + (booking.getDroneId() != null ? booking.getDroneId() : "N/A");
                    if (booking.getAssignedDrone() != null) {
                        droneText += " (" + booking.getAssignedDrone() + ")";
                    }
                    tvDroneInfo.setText(droneText);
                } else {
                    tvOperatorInfo.setVisibility(View.GONE);
                    tvDroneInfo.setVisibility(View.GONE);
                }

                String status = booking.getStatus() != null
                        ? booking.getStatus().trim()
                        : "Pending";

                tvStatus.setText(status);

                updateStatusBadge(status);
                updateTimeline(status);

                // 🔥 FIXED PAYMENT LOGIC
                handlePaymentButton(booking);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingDetailsActivity.this,
                        "Failed to load details",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= PAYMENT =================

    private void handlePaymentButton(Booking booking) {

        if (btnPayNow == null) return;

        String status = booking.getStatus();
        String paymentStatus = booking.getPaymentStatus();

        int remaining = booking.getTotalAmount() - booking.getAdvanceAmount();

        if ("Completed".equalsIgnoreCase(status)
                && !"Paid".equalsIgnoreCase(paymentStatus)) {

            btnPayNow.setVisibility(View.VISIBLE);
            btnPayNow.setText("Pay Remaining ₹" + remaining);

            btnPayNow.setOnClickListener(v -> showPaymentDialog(booking));

        } else {
            btnPayNow.setVisibility(View.GONE);
        }
    }

    private void showPaymentDialog(Booking booking) {

        int remaining = booking.getTotalAmount() - booking.getAdvanceAmount();

        new AlertDialog.Builder(this)
                .setTitle("Payment")
                .setMessage("Pay ₹" + remaining + " ?")
                .setPositiveButton("Pay Now", (dialog, which) -> {

                    reference.child("paymentStatus")
                            .setValue("Paid")
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this,
                                            "Payment Successful ✅",
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= STATUS =================

    private void updateStatusBadge(String status) {

        if (status.equalsIgnoreCase("Approved") ||
                status.equalsIgnoreCase("Completed") ||
                status.equalsIgnoreCase("Paid")) {

            tvStatus.setText("Booking succesfuly ");

        } else if (status.equalsIgnoreCase("Rejected")) {

            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);

        } else {

            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }
    }

    // ================= TIMELINE =================

    private void updateTimeline(String status) {

        resetTimeline();

        if (status.equalsIgnoreCase("Pending")) {
            activateStep(stepPending);
        }
        else if (status.equalsIgnoreCase("Approved")) {
            activateStep(stepPending);
            activateStep(stepApproved);
        }
        else if (status.equalsIgnoreCase("Assigned")) {
            activateStep(stepPending);
            activateStep(stepApproved);
            activateStep(stepAssigned);
        }
        else if (status.equalsIgnoreCase("In Progress")) {
            activateStep(stepPending);
            activateStep(stepApproved);
            activateStep(stepAssigned);
            activateStep(stepInProgress);
        }
        else if (status.equalsIgnoreCase("Completed")) {
            activateAllSteps();
        }
    }

    private void activateStep(TextView step) {

        step.setTextColor(ContextCompat.getColor(this, R.color.electric_blue));

        step.setAlpha(0f);
        step.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void activateAllSteps() {
        activateStep(stepPending);
        activateStep(stepApproved);
        activateStep(stepAssigned);
        activateStep(stepInProgress);
        activateStep(stepCompleted);
    }

    private void resetTimeline() {

        int defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray);

        stepPending.setTextColor(defaultColor);
        stepApproved.setTextColor(defaultColor);
        stepAssigned.setTextColor(defaultColor);
        stepInProgress.setTextColor(defaultColor);
        stepCompleted.setTextColor(defaultColor);
    }

    // ================= ANIMATION =================

    private void animateEntry() {

        contentCard.setTranslationY(200f);
        contentCard.setAlpha(0f);

        contentCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (reference != null && bookingListener != null) {
            reference.removeEventListener(bookingListener);
        }
    }
}
