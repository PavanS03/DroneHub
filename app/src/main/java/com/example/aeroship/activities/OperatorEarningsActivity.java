package com.example.aeroship.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Calendar;

public class OperatorEarningsActivity extends AppCompatActivity {

    private TextView tvTotalEarnings,
            tvMonthlyEarnings,
            tvCompletedMissions,
            tvOngoingMissions;

    private DatabaseReference ordersRef;
    private String operatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_earnings);

        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        tvMonthlyEarnings = findViewById(R.id.tvMonthlyEarnings);
        tvCompletedMissions = findViewById(R.id.tvCompletedMissions);
        tvOngoingMissions = findViewById(R.id.tvOngoingMissions);

        operatorId = FirebaseAuth.getInstance().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        loadEarnings();
    }

    private void loadEarnings() {

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalEarnings = 0;
                int monthlyEarnings = 0;
                int completed = 0;
                int ongoing = 0;

                long now = System.currentTimeMillis();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(now);

                int currentMonth = cal.get(Calendar.MONTH);
                int currentYear = cal.get(Calendar.YEAR);

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String opId = ds.child("assignedOperatorId")
                            .getValue(String.class);

                    if (opId == null || !opId.equals(operatorId))
                        continue;

                    String status = ds.child("status")
                            .getValue(String.class);

                    Integer price = ds.child("totalPrice")
                            .getValue(Integer.class);

                    Long completedAt = ds.child("completedAt")
                            .getValue(Long.class);

                    if ("Completed".equalsIgnoreCase(status)
                            && price != null) {

                        totalEarnings += price;
                        completed++;

                        if (completedAt != null) {

                            cal.setTimeInMillis(completedAt);

                            int month = cal.get(Calendar.MONTH);
                            int year = cal.get(Calendar.YEAR);

                            if (month == currentMonth
                                    && year == currentYear) {

                                monthlyEarnings += price;
                            }
                        }
                    }
                    else if ("Assigned".equalsIgnoreCase(status)
                            || "In Progress".equalsIgnoreCase(status)) {
                        ongoing++;
                    }
                }

                tvTotalEarnings.setText(
                        "Total Earnings\n₹" + totalEarnings);

                tvMonthlyEarnings.setText(
                        "This Month\n₹" + monthlyEarnings);

                tvCompletedMissions.setText(
                        "Completed Missions\n" + completed);

                tvOngoingMissions.setText(
                        "Ongoing Missions\n" + ongoing);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}