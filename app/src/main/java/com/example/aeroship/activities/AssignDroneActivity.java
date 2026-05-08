package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignDroneActivity extends AppCompatActivity {

    private Spinner spinnerOperators;
    private MaterialButton btnAssign;

    private DatabaseReference usersRef, ordersRef, operatorOrdersRef;

    private String bookingId;

    private final List<String> operatorNames = new ArrayList<>();
    private final List<String> operatorIds = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private boolean isPaymentDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_drone);

        spinnerOperators = findViewById(R.id.spinnerOperators);
        btnAssign = findViewById(R.id.btnAssign);

        usersRef = FirebaseDatabase.getInstance().getReference("USERS");
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        operatorOrdersRef = FirebaseDatabase.getInstance().getReference("OPERATORS");

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            Toast.makeText(this, "Invalid Booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSpinner();
        checkBookingStatus();
        setupAssignButton();
        animateCard();
    }


    private void checkBookingStatus() {

        ordersRef.child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Boolean paid = snapshot.child("advancePaid").getValue(Boolean.class);

                        isPaymentDone = paid != null && paid;

                        if (!isPaymentDone) {
                            Toast.makeText(
                                    AssignDroneActivity.this,
                                    "Payment not completed",
                                    Toast.LENGTH_LONG
                            ).show();

                            btnAssign.setEnabled(false);
                        }

                        loadOperators();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void loadOperators() {

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                operatorNames.clear();
                operatorIds.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String role = ds.child("role").getValue(String.class);

                    if (role != null && role.equalsIgnoreCase("operator")) {
                        String name = ds.child("name").getValue(String.class);
                        String opId = ds.child("id").getValue(String.class);
                        String status = ds.child("status").getValue(String.class);

                        if (name == null) name = "Unknown";
                        if (opId == null || opId.isEmpty()) opId = "No ID";
                        if (status == null) status = "available";

                        operatorNames.add(name + " (" + opId + ") - " + status.toUpperCase());
                        operatorIds.add(ds.getKey());
                    }
                }

                if (operatorNames.isEmpty()) {
                    operatorNames.add("No Operators Found");
                    btnAssign.setEnabled(false);
                } else {
                    btnAssign.setEnabled(true);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void setupSpinner() {

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                operatorNames
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerOperators.setAdapter(adapter);
    }


    private void setupAssignButton() {

        btnAssign.setOnClickListener(v -> {

            if (!isPaymentDone) {
                Toast.makeText(this,
                        "Cannot assign before payment",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (operatorIds.isEmpty()) {
                Toast.makeText(this,
                        "No operator available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int position = spinnerOperators.getSelectedItemPosition();
            String operatorId = operatorIds.get(position);

            String selectedText = operatorNames.get(position);
            String droneId = "DRONE-" + (100 + position);
            if (selectedText.contains("(") && selectedText.contains(")")) {
                int start = selectedText.indexOf("(") + 1;
                int end = selectedText.indexOf(")");
                droneId = selectedText.substring(start, end);
            }

            validateAndAssign(operatorId, droneId);
        });
    }


    private void validateAndAssign(String operatorId, String droneId) {

        ordersRef.child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String assigned = snapshot.child("assignedOperatorId")
                                .getValue(String.class);

                        if (assigned != null && !assigned.isEmpty()) {

                            Toast.makeText(
                                    AssignDroneActivity.this,
                                    "Already Assigned",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        usersRef.child(operatorId)
                                .get()
                                .addOnSuccessListener(snap -> {

                                    String operatorName = snap.child("name").getValue(String.class);
                                    String operatorPhone = snap.child("phone").getValue(String.class);
                                    
                                    if (operatorName == null) operatorName = "Operator";
                                    if (operatorPhone == null) operatorPhone = "N/A";

                                    assignOperator(operatorId, operatorName, operatorPhone, droneId);
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void assignOperator(String operatorId, String operatorName, String operatorPhone, String droneId) {

        btnAssign.setEnabled(false);
        btnAssign.setText("Assigning...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedOperatorId", operatorId);
        updates.put("assignedOperatorName", operatorName);
        updates.put("assignedOperatorPhone", operatorPhone);
        updates.put("droneId", droneId);
        updates.put("assignedDrone", "Aero-" + droneId);
        updates.put("status", "Assigned");

        ordersRef.child(bookingId)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {

                    operatorOrdersRef.child(operatorId)
                            .child("assignedOrders")
                            .child(bookingId)
                            .setValue(true);

                    usersRef.child(operatorId)
                            .child("status")
                            .setValue("busy");

                    Toast.makeText(
                            this,
                            "Operator Assigned ✅",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnAssign.setEnabled(true);
                    btnAssign.setText("Assign Now");

                    Toast.makeText(
                            this,
                            "Assignment Failed",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    private void animateCard() {

        findViewById(R.id.assignCard)
                .setTranslationY(200f);

        findViewById(R.id.assignCard)
                .animate()
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}