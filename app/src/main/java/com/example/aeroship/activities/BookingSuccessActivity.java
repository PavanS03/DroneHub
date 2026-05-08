package com.example.aeroship.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BookingSuccessActivity extends AppCompatActivity {

    private MaterialCardView successCard;
    private ImageView imgSuccess;
    private MaterialButton btnGoDashboard;

    private TextView tvBookingId, tvTransactionId;

    private boolean isNavigated = false;

    private String bookingId; // ✅ GLOBAL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        successCard = findViewById(R.id.successCard);
        imgSuccess = findViewById(R.id.imgSuccess);
        btnGoDashboard = findViewById(R.id.btnGoDashboard);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvTransactionId = findViewById(R.id.tvTransactionId);

        // ================= RECEIVE DATA =================
        bookingId = getIntent().getStringExtra("bookingId");
        String transactionId = getIntent().getStringExtra("transactionId");

        if (bookingId != null) {
            tvBookingId.setText("Booking ID: " + bookingId);

            // ✅ NEW: SAVE bookingId locally
            saveBookingId(bookingId);
        }

        if (transactionId != null) {
            tvTransactionId.setText("Transaction ID: " + transactionId);
        }

        startAnimation();

        btnGoDashboard.setOnClickListener(v -> {
            animateButton(v);
            goToDashboard();
        });

        new Handler().postDelayed(() -> {
            if (!isNavigated) {
                goToDashboard();
            }
        }, 5000);

        // Back press
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goToDashboard();
                    }
                });
    }

    // ================= SAVE BOOKING =================
    private void saveBookingId(String id) {
        SharedPreferences prefs = getSharedPreferences("AEROSHIP_PREFS", MODE_PRIVATE);
        prefs.edit().putString("LAST_BOOKING_ID", id).apply();
    }

    private void startAnimation() {

        successCard.setScaleX(0.9f);
        successCard.setScaleY(0.9f);
        successCard.setAlpha(0f);

        successCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        imgSuccess.setScaleX(0f);
        imgSuccess.setScaleY(0f);

        imgSuccess.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateButton(android.view.View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start())
                .start();
    }

    private void goToDashboard() {

        if (isNavigated) return;
        isNavigated = true;

        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("bookingId", bookingId);
        startActivity(intent);
        finish();
    }
}