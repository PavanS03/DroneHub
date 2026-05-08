package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.firebase.database.*;

public class TrackDroneActivity extends AppCompatActivity {

    private TextView tvStatus, step1, step2, step3, step4;
    private TextView tvOperatorName, tvDroneId;
    private Button btnPayRemaining;

    private DatabaseReference ordersRef;
    private ValueEventListener trackingListener;

    private String bookingId;

    private ObjectAnimator scaleXAnim, scaleYAnim;
    private String lastStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_drone);

        initViews();

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null || bookingId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid Booking ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        loadTrackingData();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);
        tvOperatorName = findViewById(R.id.tvOperatorName);
        tvDroneId = findViewById(R.id.tvDroneId);

        btnPayRemaining = findViewById(R.id.btnPayRemaining);

        if (btnPayRemaining != null) {
            btnPayRemaining.setVisibility(View.GONE);
        }
    }

    private void loadTrackingData() {

        trackingListener = ordersRef.child(bookingId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(TrackDroneActivity.this,
                                    "Booking not found", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Booking booking = snapshot.getValue(Booking.class);

                        if (booking == null) {
                            Toast.makeText(TrackDroneActivity.this,
                                    "Data error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String status = booking.getStatus() != null
                                ? booking.getStatus().trim()
                                : "Pending";

                        tvStatus.setText(status);

                        updateStatusBadge(status);
                        updateTimeline(status);
                        startBadgeAnimation();

                        if (!status.equals(lastStatus)) {
                            onStatusChanged(status);
                            lastStatus = status;
                        }

                        String paymentStatus = booking.getPaymentStatus() != null
                                ? booking.getPaymentStatus()
                                : "";

                        if (status.equalsIgnoreCase("Completed")
                                && paymentStatus.equalsIgnoreCase("Partial")) {

                            btnPayRemaining.setVisibility(View.VISIBLE);

                            btnPayRemaining.setOnClickListener(v -> {

                                Intent intent = new Intent(
                                        TrackDroneActivity.this,
                                        FinalPaymentActivity.class);

                                intent.putExtra("bookingId", bookingId);
                                intent.putExtra("remainingAmount",
                                        booking.getRemainingAmount());

                                startActivity(intent);
                            });
                        } else {
                            btnPayRemaining.setVisibility(View.GONE);
                        }

                        String operator = booking.getAssignedOperatorName() != null
                                ? booking.getAssignedOperatorName()
                                : "Not Assigned";

                        tvOperatorName.setText("Operator: " + operator);

                        String drone = booking.getDroneId() != null
                                ? booking.getDroneId()
                                : "Pending";

                        tvDroneId.setText("Drone ID: " + drone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrackDroneActivity.this,
                                "Tracking failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onStatusChanged(String status) {
        Toast.makeText(this, "Status Updated: " + status, Toast.LENGTH_SHORT).show();

        tvStatus.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() ->
                        tvStatus.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start()
                ).start();
    }

    private void updateStatusBadge(String status) {

        if (status.equalsIgnoreCase("Completed")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
        } else if (status.equalsIgnoreCase("Assigned") ||
                status.equalsIgnoreCase("Approved")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        } else if (status.equalsIgnoreCase("Rejected")) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }
    }

    private void updateTimeline(String status) {

        int active = ContextCompat.getColor(this, R.color.electric_blue);
        int inactive = ContextCompat.getColor(this, android.R.color.darker_gray);

        step1.setTextColor(active);
        step2.setTextColor(inactive);
        step3.setTextColor(inactive);
        step4.setTextColor(inactive);

        if (status.equalsIgnoreCase("Approved")) {
            step2.setTextColor(active);
            animateStep(step2);
        } else if (status.equalsIgnoreCase("Assigned")) {
            step2.setTextColor(active);
            step3.setTextColor(active);
            animateStep(step3);
        } else if (status.equalsIgnoreCase("Completed")) {
            step2.setTextColor(active);
            step3.setTextColor(active);
            step4.setTextColor(active);
            animateStep(step4);
        }
    }

    private void animateStep(TextView step) {
        step.setAlpha(0f);
        step.setScaleX(0.8f);
        step.setScaleY(0.8f);

        step.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void startBadgeAnimation() {
        stopBadgeAnimation();

        scaleXAnim = ObjectAnimator.ofFloat(tvStatus, "scaleX", 1f, 1.08f, 1f);
        scaleYAnim = ObjectAnimator.ofFloat(tvStatus, "scaleY", 1f, 1.08f, 1f);

        scaleXAnim.setDuration(900);
        scaleYAnim.setDuration(900);

        scaleXAnim.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnim.setRepeatCount(ValueAnimator.INFINITE);

        scaleXAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleXAnim.start();
        scaleYAnim.start();
    }

    private void stopBadgeAnimation() {
        if (scaleXAnim != null) scaleXAnim.cancel();
        if (scaleYAnim != null) scaleYAnim.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopBadgeAnimation();

        if (ordersRef != null && trackingListener != null) {
            ordersRef.removeEventListener(trackingListener);
        }
    }
}
