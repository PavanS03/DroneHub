package com.example.aeroship.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import android.widget.TextView;

public class BookingStatuActivity extends AppCompatActivity {

    private TextView tvBookingDetails, tvAmount, tvStatus, tvOperator;
    private MaterialButton btnPayNow;
    private MaterialCardView statusCard;

    private DatabaseReference reference;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_statu);

        initViews();
        startPremiumAnimation();

        orderId = getIntent().getStringExtra("orderId");

        if (orderId != null) {
            reference = FirebaseDatabase.getInstance().getReference("ORDERS");
            listenForUpdates();
        }
    }

    private void initViews() {
        tvBookingDetails = findViewById(R.id.tvBookingDetails);
        tvAmount = findViewById(R.id.tvAmount);
        tvStatus = findViewById(R.id.tvStatus);
        tvOperator = findViewById(R.id.tvOperator);
        btnPayNow = findViewById(R.id.btnPayNow);
        statusCard = findViewById(R.id.statusCard);
    }

    private void startPremiumAnimation() {

        statusCard.setAlpha(0f);
        statusCard.setTranslationY(200f);

        statusCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .start();
    }

    private void listenForUpdates() {

        reference.child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) return;

                        String sector = snapshot.child("sector").getValue(String.class);
                        String level = snapshot.child("level").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);
                        String operatorName = snapshot.child("assignedOperatorName").getValue(String.class);
                        String operatorPhone = snapshot.child("assignedOperatorPhone").getValue(String.class);
                        Integer amount = snapshot.child("totalAmount").getValue(Integer.class);

                        updateUI(sector, level, date, location, status, operatorName, operatorPhone, amount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void updateUI(String sector,
                          String level,
                          String date,
                          String location,
                          String status,
                          String operatorName,
                          String operatorPhone,
                          Integer amount) {

        if (status == null) status = "Pending";
        if (amount == null) amount = 0;

        tvBookingDetails.setText(
                "Sector: " + sector + "\n" +
                        "Level: " + level + "\n" +
                        "Date: " + date + "\n" +
                        "Location: " + location
        );

        tvAmount.setText("Total Amount: ₹" + amount);
        tvStatus.setText("Status: " + status);

        switch (status) {

            case "Approved":
                tvStatus.setTextColor(Color.GREEN);
                btnPayNow.setVisibility(View.VISIBLE);
                break;

            case "Rejected":
                tvStatus.setTextColor(Color.RED);
                btnPayNow.setVisibility(View.GONE);
                break;

            case "Paid":
                tvStatus.setTextColor(Color.CYAN);
                btnPayNow.setVisibility(View.GONE);
                break;

            default:
                tvStatus.setTextColor(Color.YELLOW);
                btnPayNow.setVisibility(View.GONE);
        }

        if (operatorName != null) {
            tvOperator.setText("Operator: " + operatorName +
                    "\nPhone: " + operatorPhone);
        } else {
            tvOperator.setText("Operator will be assigned after approval.");
        }

        btnPayNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });
    }
}