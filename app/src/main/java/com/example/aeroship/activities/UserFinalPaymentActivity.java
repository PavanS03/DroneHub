package com.example.aeroship.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserFinalPaymentActivity extends AppCompatActivity {

    private TextView tvAmount, tvTitle;
    private MaterialButton btnPay;

    private DatabaseReference ordersRef;

    private String bookingId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_final_payment);


        tvAmount = findViewById(R.id.tvAmount);
        tvTitle = findViewById(R.id.tvTitle);
        btnPay = findViewById(R.id.btnPay);

        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");


        bookingId = getIntent().getStringExtra("bookingId");
        amount = getIntent().getDoubleExtra("amount", 0);

        if (bookingId == null) {
            Toast.makeText(this, "Invalid Booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        tvTitle.setText("Complete Your Payment");
        tvAmount.setText("₹ " + amount);

        // ================= PAYMENT BUTTON =================

        btnPay.setOnClickListener(v -> {

            btnPay.setEnabled(false);
            btnPay.setText("Processing...");

            processPayment();
        });
    }


    private void processPayment() {


        ordersRef.child(bookingId)
                .child("paymentStatus")
                .setValue("Fully Paid")
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            UserFinalPaymentActivity.this,
                            "Payment Successful ✅",
                            Toast.LENGTH_LONG
                    ).show();

                    btnPay.setText("Paid ✅");

                    goToRatingScreen();
                })

                .addOnFailureListener(e -> {

                    btnPay.setEnabled(true);
                    btnPay.setText("Pay Now");

                    Toast.makeText(
                            UserFinalPaymentActivity.this,
                            "Payment Failed ❌",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    private void goToRatingScreen() {

        btnPay.postDelayed(() -> {

            startActivity(
                    new android.content.Intent(
                            UserFinalPaymentActivity.this,
                            RatingActivity.class
                    ).putExtra("bookingId", bookingId)
            );

            finish();

        }, 1200);
    }
}