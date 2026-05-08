package com.example.aeroship.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class OperatorMissionDetailActivity extends AppCompatActivity {

    private TextView tvUser, tvSector, tvLevel,
            tvLocation, tvPrice, tvStatus;

    private MaterialButton btnStart, btnComplete;

    private DatabaseReference ordersRef, usersRef;

    private String bookingId;
    private String operatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_mission_detail);

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            finish();
            return;
        }

        operatorId = FirebaseAuth.getInstance().getUid();

        initViews();
        initFirebase();
        loadMissionDetails();
    }

    private void initViews() {
        tvUser = findViewById(R.id.tvUser);
        tvSector = findViewById(R.id.tvSector);
        tvLevel = findViewById(R.id.tvLevel);
        tvLocation = findViewById(R.id.tvLocation);
        tvPrice = findViewById(R.id.tvPrice);
        tvStatus = findViewById(R.id.tvStatus);

        btnStart = findViewById(R.id.btnStart);
        btnComplete = findViewById(R.id.btnComplete);
    }

    private void initFirebase() {
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");
    }

    private void loadMissionDetails() {
        ordersRef.child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String user = snapshot.child("userName").getValue(String.class);
                        String sector = snapshot.child("sector").getValue(String.class);
                        String level = snapshot.child("level").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        
                        Object amountObj = snapshot.child("totalAmount").getValue();
                        String price = (amountObj != null) ? String.valueOf(amountObj) : "0";
                        
                        String status = snapshot.child("status").getValue(String.class);

                        tvUser.setText("User: " + (user != null ? user : "Unknown"));
                        tvSector.setText("Sector: " + (sector != null ? sector : "N/A"));
                        tvLevel.setText("Level: " + (level != null ? level : "N/A"));
                        tvLocation.setText("Location: " + (location != null ? location : "N/A"));
                        tvPrice.setText("Total Price: ₹" + price);
                        tvStatus.setText("Status: " + (status != null ? status : "Pending"));

                        setupButtons(status);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupButtons(String status) {
        if ("Assigned".equalsIgnoreCase(status)) {
            btnStart.setEnabled(true);
            btnComplete.setEnabled(false);
            btnStart.setOnClickListener(v -> updateStatus("In Progress"));
        } else if ("In Progress".equalsIgnoreCase(status)) {
            btnStart.setEnabled(false);
            btnComplete.setEnabled(true);
            btnComplete.setOnClickListener(v -> updateStatus("Completed"));
        } else {
            btnStart.setEnabled(false);
            btnComplete.setEnabled(false);
        }
    }

    private void updateStatus(String newStatus) {
        ordersRef.child(bookingId)
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    if ("Completed".equals(newStatus)) {
                        usersRef.child(operatorId)
                                .child("status")
                                .setValue("available");

                        releaseInventoryOnCompletion();
                    }

                    Toast.makeText(this, "Mission " + newStatus, Toast.LENGTH_SHORT).show();
                    loadMissionDetails();
                });
    }

    private void releaseInventoryOnCompletion() {
        ordersRef.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sector = snapshot.child("sector").getValue(String.class);
                Integer droneCount = snapshot.child("droneCount").getValue(Integer.class);
                Boolean released = snapshot.child("inventoryReleased").getValue(Boolean.class);

                if (Boolean.TRUE.equals(released)) return;

                // 1. Mark as released to avoid double-incrementing
                ordersRef.child(bookingId).child("inventoryReleased").setValue(true);

                // 2. Increment Sector Inventory
                if (sector != null && droneCount != null) {
                    DatabaseReference sectorRef = FirebaseDatabase.getInstance().getReference("SECTORS").child(sector);
                    sectorRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Long available = currentData.child("droneCount").getValue(Long.class);
                            Long max = currentData.child("maxDrones").getValue(Long.class);
                            if (available != null) {
                                long newCount = available + droneCount;
                                if (max != null && newCount > max) newCount = max;
                                currentData.child("droneCount").setValue(newCount);
                            }
                            return Transaction.success(currentData);
                        }
                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}