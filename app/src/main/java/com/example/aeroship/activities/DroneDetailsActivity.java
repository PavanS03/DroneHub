package com.example.aeroship.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;

public class DroneDetailsActivity extends AppCompatActivity {

    private ImageView imgDrone, btnBack;
    private TextView tvDroneName, tvSector, tvLevel, tvFeatures, tvPrice;
    private Spinner spinnerDuration;
    private View contentLayout;
    private Button btnBookNow;

    private String sector = "", level = "";

    private int pricePerHour = 2000;
    private int totalPrice = 2000;
    private int selectedHours = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_details);

        Intent intent = getIntent();
        if (intent != null) {
            sector = intent.getStringExtra("sector_name");
            level = intent.getStringExtra("drone_level");

            pricePerHour = intent.getIntExtra("amount", 2000);
            totalPrice = pricePerHour;
        }

        if (sector == null) sector = "Unknown Sector";
        if (level == null) level = "Standard";

        initViews();
        setupDurationSpinner();
        animateEntry();
    }

    private void initViews() {

        imgDrone = findViewById(R.id.imgDrone);
        btnBack = findViewById(R.id.btnBack);
        tvDroneName = findViewById(R.id.tvDroneName);
        tvSector = findViewById(R.id.tvSector);
        tvLevel = findViewById(R.id.tvLevel);
        tvFeatures = findViewById(R.id.tvFeatures);
        tvPrice = findViewById(R.id.tvPrice);
        spinnerDuration = findViewById(R.id.spinnerDuration);
        contentLayout = findViewById(R.id.contentLayout);
        btnBookNow = findViewById(R.id.btnBookNow);

        if (tvDroneName != null)
            tvDroneName.setText(level + " Drone Package");

        if (tvSector != null)
            tvSector.setText("Sector: " + sector);

        if (tvLevel != null)
            tvLevel.setText("Level: " + level);

        if (tvFeatures != null)
            tvFeatures.setText(
                    "• Ultra Stabilized Camera\n" +
                            "• Smart GPS Navigation\n" +
                            "• Real-time Monitoring\n" +
                            "• Safety Auto Return\n" +
                            "• Professional Pilot Included"
            );

        if (tvPrice != null)
            tvPrice.setText("₹ " + totalPrice);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {

                Intent intent = new Intent(
                        DroneDetailsActivity.this,
                        PlaceOrderActivity.class);

                intent.putExtra("sector", sector);
                intent.putExtra("level", level);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("pricePerHour", pricePerHour);
                intent.putExtra("hours", selectedHours);

                startActivity(intent);
            });
        }
    }

    private void setupDurationSpinner() {

        if (spinnerDuration == null) return;

        String[] durations = {"1 Hour", "2 Hours", "3 Hours", "4 Hours", "8 Hours"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                durations
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinnerDuration.setAdapter(adapter);

        spinnerDuration.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position == 4) {
                            selectedHours = 8;
                        } else {
                            selectedHours = position + 1;
                        }

                        int newPrice = pricePerHour * selectedHours;

                        animatePriceChange(totalPrice, newPrice);
                        totalPrice = newPrice;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    private void animatePriceChange(int oldPrice, int newPrice) {

        if (tvPrice == null) return;

        ValueAnimator animator = ValueAnimator.ofInt(oldPrice, newPrice);
        animator.setDuration(400);

        animator.addUpdateListener(animation ->
                tvPrice.setText("₹ " + animation.getAnimatedValue()));

        animator.start();
    }

    private void animateEntry() {

        if (contentLayout == null) return;

        contentLayout.setAlpha(0f);
        contentLayout.setTranslationY(200f);

        contentLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}