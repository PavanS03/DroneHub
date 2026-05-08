package com.example.aeroship.activities;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceOrderActivity extends AppCompatActivity {

    private TextView tvSummary, tvTotal, tvDroneCount, tvAvailability;
    private EditText etLocation, etDate, etStartTime, etEndTime;
    private ImageView btnBack;
    private View btnPlus, btnMinus, btnPlaceOrder;

    private String sector = "", level = "";
    private int pricePerHour = 0;
    private int droneCount = 1;
    private int hours = 1;

    private int currentAvailable = 0;
    private int levelMax = 0;

    private int totalAmount = 0;
    private int advanceAmount = 500;
    private int remainingAmount = 0;
    private int lastAnimatedTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        initViews();
        loadIntentData();
        setupUI();
        animateEntry();
        fetchSectorLimit();
    }

    private void initViews() {
        tvSummary = findViewById(R.id.tvSummary);
        tvTotal = findViewById(R.id.tvTotal);
        tvDroneCount = findViewById(R.id.tvDroneCount);
        tvAvailability = findViewById(R.id.tvAvailability);

        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);

        btnBack = findViewById(R.id.btnBack);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            sector = intent.getStringExtra("sector");
            level = intent.getStringExtra("level");
            pricePerHour = intent.getIntExtra("pricePerHour", 2500);
            hours = intent.getIntExtra("hours", 1);
        }

        if (sector == null) sector = "";
        if (level == null) level = "";

        tvDroneCount.setText(String.valueOf(droneCount));
        updateTotalAnimated();
    }

    private void fetchSectorLimit() {
        DatabaseReference levelsRef = FirebaseDatabase.getInstance()
                .getReference("SECTORS")
                .child(sector)
                .child("levels")
                .child(level);

        levelsRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Long count = snapshot.child("count").getValue(Long.class);
                Long max = snapshot.child("max").getValue(Long.class);
                currentAvailable = count != null ? count.intValue() : 0;
                levelMax = max != null ? max.intValue() : 0;
            }
            
            if (levelMax == 0) {
                if (level.equalsIgnoreCase("Low")) levelMax = 4;
                else if (level.equalsIgnoreCase("Medium")) levelMax = 3;
                else if (level.equalsIgnoreCase("High")) levelMax = 3;
            }
            
            updateAvailabilityUI();
        });
    }

    private void updateAvailabilityUI() {
        tvAvailability.setText("Available Drones: " + currentAvailable + " / " + levelMax);
        btnPlaceOrder.setEnabled(currentAvailable > 0);
        
        if (droneCount >= currentAvailable) {
            btnPlus.setEnabled(false);
            btnPlus.setAlpha(0.5f);
        } else {
            btnPlus.setEnabled(true);
            btnPlus.setAlpha(1.0f);
        }
    }

    private void setupUI() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) ->
                            etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });

        TextWatcher timeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateHours();
            }
        };

        etStartTime.addTextChangedListener(timeWatcher);
        etEndTime.addTextChangedListener(timeWatcher);

        btnPlus.setOnClickListener(v -> {
            if (droneCount < currentAvailable) {
                droneCount++;
                tvDroneCount.setText(String.valueOf(droneCount));
                updateTotalAnimated();
                updateAvailabilityUI();
            } else {
                Toast.makeText(this, "No more drones available", Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (droneCount > 1) {
                droneCount--;
                tvDroneCount.setText(String.valueOf(droneCount));
                updateTotalAnimated();
                updateAvailabilityUI();
            }
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void calculateHours() {
        String s = etStartTime.getText().toString().trim().toUpperCase();
        String e = etEndTime.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(s) || TextUtils.isEmpty(e)) return;

        int startMin = timeToMinutes(s);
        int endMin = timeToMinutes(e);

        if (startMin != -1 && endMin != -1) {
            int diffMin = endMin - startMin;
            if (diffMin <= 0) diffMin += 24 * 60; 

            int calculatedHours = (int) Math.ceil(diffMin / 60.0);
            if (calculatedHours < 1) calculatedHours = 1;

            if (this.hours != calculatedHours) {
                this.hours = calculatedHours;
                updateTotalAnimated();
            }
        }
    }

    private int timeToMinutes(String time) {
        Pattern p = Pattern.compile("(\\d{1,2})(?:[:.](\\d{2}))?\\s*([AP]M)");
        Matcher m = p.matcher(time);
        if (m.find()) {
            int hh = Integer.parseInt(m.group(1));
            int mm = (m.group(2) != null) ? Integer.parseInt(m.group(2)) : 0;
            String ampm = m.group(3);

            if (ampm.equals("PM") && hh < 12) hh += 12;
            if (ampm.equals("AM") && hh == 12) hh = 0;
            return hh * 60 + mm;
        }
        return -1;
    }

    private void updateTotalAnimated() {
        int newTotal = pricePerHour * hours * droneCount;
        totalAmount = newTotal;
        remainingAmount = totalAmount - advanceAmount;
        if (remainingAmount < 0) remainingAmount = 0;

        ValueAnimator animator = ValueAnimator.ofInt(lastAnimatedTotal, totalAmount);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            tvTotal.setText("Total: ₹ " + val);
        });
        animator.start();
        lastAnimatedTotal = totalAmount;

        updateSummary();
    }

    private void updateSummary() {
        tvSummary.setText("Sector: " + sector + "\nLevel: " + level + "\nDuration: " + hours + " Hour(s)");
    }

    private void placeOrder() {
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();

        if (TextUtils.isEmpty(location)) { etLocation.setError("Required"); return; }
        if (TextUtils.isEmpty(date)) { etDate.setError("Required"); return; }
        if (TextUtils.isEmpty(startTime)) { etStartTime.setError("Required"); return; }
        if (TextUtils.isEmpty(endTime)) { etEndTime.setError("Required"); return; }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookingId = UUID.randomUUID().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(userId);
        booking.setSector(sector);
        booking.setLevel(level);
        booking.setDuration(hours + " Hour(s)");
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setLocation(location);
        booking.setDroneCount(droneCount);
        booking.setTotalAmount(totalAmount);
        booking.setAdvanceAmount(advanceAmount);
        booking.setRemainingAmount(remainingAmount);
        booking.setPaymentStatus("Unpaid");
        booking.setPaymentStage("ADVANCE_PENDING");
        booking.setStatus("Pending");
        booking.setTimestamp(System.currentTimeMillis());

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("USERS").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        booking.setUserName(name != null ? name : "Customer");
                        booking.setUserPhone(phone != null ? phone : "");
                        
                        performAtomicBooking(booking);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        booking.setUserName("Customer");
                        performAtomicBooking(booking);
                    }
                });
    }

    private void performAtomicBooking(Booking booking) {
        DatabaseReference levelRef = FirebaseDatabase.getInstance()
                .getReference("SECTORS")
                .child(sector)
                .child("levels")
                .child(level);

        levelRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long available = currentData.child("count").getValue(Long.class);
                
                if (available == null || available < droneCount) {
                    return Transaction.abort(); 
                }

                currentData.child("count").setValue(available - droneCount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    updateGlobalSectorCount();
                    saveOrder(booking);
                } else {
                    Toast.makeText(PlaceOrderActivity.this, 
                        "Sorry, drones just became unavailable!", Toast.LENGTH_LONG).show();
                    btnPlaceOrder.setEnabled(false);
                }
            }
        });
    }

    private void updateGlobalSectorCount() {
        DatabaseReference sectorRef = FirebaseDatabase.getInstance()
                .getReference("SECTORS")
                .child(sector);
        
        sectorRef.child("levels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = 0;
                for (DataSnapshot levelSnap : snapshot.getChildren()) {
                    Long count = levelSnap.child("count").getValue(Long.class);
                    if (count != null) total += count;
                }
                sectorRef.child("droneCount").setValue(total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveOrder(Booking booking) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("ORDERS")
                .child(booking.getBookingId());

        orderRef.setValue(booking).addOnSuccessListener(aVoid -> {
            Toast.makeText(PlaceOrderActivity.this, "Order Placed! Please pay advance.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PlaceOrderActivity.this, PaymentActivity.class)
                    .putExtra("bookingId", booking.getBookingId())
                    .putExtra("totalAmount", totalAmount));
            finish();
        }).addOnFailureListener(e -> {
            rollbackInventory(booking.getDroneCount());
            Toast.makeText(this, "Booking Failed. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

    private void rollbackInventory(int count) {
        DatabaseReference levelRef = FirebaseDatabase.getInstance()
                .getReference("SECTORS")
                .child(sector)
                .child("levels")
                .child(level);
        
        levelRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long available = currentData.child("count").getValue(Long.class);
                if (available != null) {
                    currentData.child("count").setValue(available + count);
                }
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                updateGlobalSectorCount();
            }
        });
    }

    private void animateEntry() {
        View content = findViewById(R.id.contentLayout);
        if (content == null) return;
        content.setAlpha(0f);
        content.setTranslationY(200f);
        content.animate().alpha(1f).translationY(0f).setDuration(600).setInterpolator(new DecelerateInterpolator()).start();
    }
}
