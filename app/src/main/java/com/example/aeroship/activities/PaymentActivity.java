package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvTotalAmount, tvStatus;
    private MaterialButton btnPayNow;
    private MaterialCardView paymentCard;
    private ProgressBar progressPayment;

    private String bookingId;

    private int originalAmount;
    private int totalAmount;

    private DatabaseReference orderRef;

    private static final int ADVANCE_AMOUNT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvStatus = findViewById(R.id.tvStatus);
        btnPayNow = findViewById(R.id.btnPayNow);
        paymentCard = findViewById(R.id.paymentCard);
        progressPayment = findViewById(R.id.progressPayment);

        orderRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        bookingId = getIntent().getStringExtra("bookingId");

        originalAmount = getIntent().getIntExtra("totalAmount", 0);

        totalAmount = ADVANCE_AMOUNT;

        tvTotalAmount.setText("Advance Amount: ₹" + totalAmount);

        animateEntry();

        btnPayNow.setOnClickListener(v -> {
            animateButton(v);
            processPayment();
        });
    }

    private void animateEntry() {
        paymentCard.setAlpha(0f);
        paymentCard.setTranslationY(300f);

        paymentCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start())
                .start();
    }

    private void processPayment() {

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking Error", Toast.LENGTH_SHORT).show();
            return;
        }

        progressPayment.setVisibility(View.VISIBLE);
        btnPayNow.setEnabled(false);

        if (tvStatus != null) {
            tvStatus.setText("🔍 Scanning QR...");
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (tvStatus != null) {
                tvStatus.setText("⏳ Waiting for confirmation...");
            }

            paymentCard.animate().alpha(0.7f).setDuration(500).start();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                String transactionId = UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

                updateBooking(transactionId);

            }, 2000);

        }, 3000);
    }

    private void updateBooking(String transactionId) {

        int remainingAmount = originalAmount - ADVANCE_AMOUNT;

        if (remainingAmount < 0) remainingAmount = 0;

        HashMap<String, Object> updateMap = new HashMap<>();

        updateMap.put("advancePaid", true);
        updateMap.put("paymentStage", "ADVANCE_PAID");
        updateMap.put("paymentStatus", "Partial"); 
        updateMap.put("transactionId", transactionId);

        updateMap.put("totalAmount", originalAmount);   
        updateMap.put("advanceAmount", ADVANCE_AMOUNT); 
        updateMap.put("remainingAmount", remainingAmount); 

        updateMap.put("timestamp", System.currentTimeMillis());

        orderRef.child(bookingId)
                .updateChildren(updateMap)
                .addOnSuccessListener(unused -> {

                    progressPayment.setVisibility(View.GONE);

                    Intent intent = new Intent(PaymentActivity.this, QRPaymentActivity.class);
                    intent.putExtra("bookingId", bookingId);
                    intent.putExtra("transactionId", transactionId);
                    intent.putExtra("amount", totalAmount); 

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressPayment.setVisibility(View.GONE);
                    btnPayNow.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                });
    }
}
