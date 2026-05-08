package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DeliveryStatusActivity extends AppCompatActivity {

    private TextView tvStatus, tvCompletionTime;
    private MaterialButton btnCompleteDelivery;
    private MaterialCardView mainCard;

    private DatabaseReference ordersRef;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        tvStatus = findViewById(R.id.tvStatus);
        tvCompletionTime = findViewById(R.id.tvCompletionTime);
        btnCompleteDelivery = findViewById(R.id.btnCompleteDelivery);
        mainCard = findViewById(R.id.mainCard);

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            finish();
            return;
        }

        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        animateEntry();

        btnCompleteDelivery.setOnClickListener(v -> completeDelivery());
    }

    private void completeDelivery() {

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("status", "Completed");
        updateMap.put("completionTime", System.currentTimeMillis());

        ordersRef.child(bookingId)
                .updateChildren(updateMap)
                .addOnSuccessListener(unused -> {

                    tvStatus.setText("Completed");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_approved);

                    String time = new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    ).format(new Date());

                    tvCompletionTime.setText("Completed at: " + time);

                    btnCompleteDelivery.setVisibility(View.GONE);

                    animateBadge();

                    Toast.makeText(this,
                            "Mission Completed Successfully",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void animateEntry() {

        mainCard.setTranslationY(200f);
        mainCard.setAlpha(0f);

        mainCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateBadge() {

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(tvStatus, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY =
                ObjectAnimator.ofFloat(tvStatus, "scaleY", 1f, 1.1f, 1f);

        scaleX.setDuration(600);
        scaleY.setDuration(600);

        scaleX.start();
        scaleY.start();
    }
}