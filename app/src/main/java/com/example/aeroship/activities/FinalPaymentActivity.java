package com.example.aeroship.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.UUID;

public class FinalPaymentActivity extends AppCompatActivity {

    private TextView tvRemainingAmount, tvStatus;
    private MaterialButton btnPayNow;
    private ProgressBar progressBar;
    private MaterialCardView card;

    private String bookingId;
    private int remainingAmount;

    private DatabaseReference orderRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_payment);

        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        tvStatus = findViewById(R.id.tvStatus);
        btnPayNow = findViewById(R.id.btnPayNow);
        progressBar = findViewById(R.id.progressPayment);
        card = findViewById(R.id.paymentCard);

        orderRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");

        bookingId = getIntent().getStringExtra("bookingId");
        remainingAmount = getIntent().getIntExtra("remainingAmount", 0);

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Invalid Booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvRemainingAmount.setText("Remaining Amount: ₹" + remainingAmount);

        animateEntry();

        btnPayNow.setOnClickListener(v -> processFinalPayment());
    }

    private void animateEntry() {
        card.setAlpha(0f);
        card.setTranslationY(250f);

        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void processFinalPayment() {
        progressBar.setVisibility(View.VISIBLE);
        btnPayNow.setEnabled(false);

        if (tvStatus != null) {
            tvStatus.setText("⏳ Processing...");
            tvStatus.setTextColor(Color.parseColor("#FF9800"));
        }

        btnPayNow.postDelayed(() -> {
            String transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            HashMap<String, Object> map = new HashMap<>();
            map.put("paymentStatus", "Fully Paid");
            map.put("paymentStage", "FINAL_PAID");
            map.put("remainingPaid", true);
            map.put("remainingAmount", 0);
            map.put("finalTransactionId", transactionId);
            map.put("status", "Completed");
            map.put("timestamp", System.currentTimeMillis());

            orderRef.child(bookingId).updateChildren(map)
                    .addOnSuccessListener(unused -> {
                        completeOrderTransaction();

                        progressBar.setVisibility(View.GONE);
                        if (tvStatus != null) {
                            tvStatus.setText("✅ Paid");
                            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                        }

                        Toast.makeText(this, "Payment Successful 🎉", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, BookingSuccessActivity.class);
                        intent.putExtra("bookingId", bookingId);
                        intent.putExtra("transactionId", transactionId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnPayNow.setEnabled(true);
                        if (tvStatus != null) {
                            tvStatus.setText("❌ Failed");
                            tvStatus.setTextColor(Color.RED);
                        }
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                    });
        }, 1500);
    }

    private void completeOrderTransaction() {
        orderRef.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String operatorId = snapshot.child("assignedOperatorId").getValue(String.class);
                String sector = snapshot.child("sector").getValue(String.class);
                Integer droneCount = snapshot.child("droneCount").getValue(Integer.class);
                Boolean released = snapshot.child("inventoryReleased").getValue(Boolean.class);

                if (Boolean.TRUE.equals(released)) return;

                orderRef.child(bookingId).child("inventoryReleased").setValue(true);

                if (operatorId != null) {
                    usersRef.child(operatorId).child("status").setValue("available");
                }

                if (sector != null && droneCount != null) {
                    incrementInventory(sector, droneCount);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void incrementInventory(String sector, int count) {
        DatabaseReference sectorRef = FirebaseDatabase.getInstance().getReference("SECTORS").child(sector);
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
}
