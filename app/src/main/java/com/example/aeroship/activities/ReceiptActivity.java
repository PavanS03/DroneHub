package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReceiptActivity extends AppCompatActivity {

    private TextView tvOrderId, tvUserName, tvDroneId, tvSector, tvTimeRange, tvTotalCost, tvAdvancePaid, tvRemainingAmount;
    private Button btnDone;
    private DatabaseReference orderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        initViews();
        String bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId != null) {
            fetchOrderDetails(bookingId);
        } else {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDone.setOnClickListener(v -> {
            String role = getSharedPreferences("AEROSHIP_PREFS", MODE_PRIVATE).getString("user_role", "Customer");
            Intent intent;
            if ("Operator".equalsIgnoreCase(role)) {
                intent = new Intent(ReceiptActivity.this, OperatorDashboardActivity.class);
            } else {
                intent = new Intent(ReceiptActivity.this, CustomerDashboardActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvUserName = findViewById(R.id.tvUserName);
        tvDroneId = findViewById(R.id.tvDroneId);
        tvSector = findViewById(R.id.tvSector);
        tvTimeRange = findViewById(R.id.tvTimeRange);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvAdvancePaid = findViewById(R.id.tvAdvancePaid);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        btnDone = findViewById(R.id.btnDone);
    }

    private void fetchOrderDetails(String orderId) {
        orderRef = FirebaseDatabase.getInstance().getReference("ORDERS").child(orderId);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {
                        displayReceipt(booking);
                    }
                } else {
                    Toast.makeText(ReceiptActivity.this, "Receipt not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiptActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayReceipt(Booking booking) {
        if (booking == null) return;
        
        String bId = booking.getBookingId();
        tvOrderId.setText("Order ID: #" + (bId != null && bId.length() > 8 ? bId.substring(0, 8).toUpperCase() : bId));
        
        String name = booking.getUserName();
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("null")) {
            String uid = booking.getUserId();
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("USERS").child(uid).child("name")
                    .get().addOnSuccessListener(snapshot -> {
                        String fetchedName = snapshot.getValue(String.class);
                        if (fetchedName != null) {
                            tvUserName.setText("User: " + fetchedName);
                            FirebaseDatabase.getInstance().getReference("ORDERS").child(bId).child("userName").setValue(fetchedName);
                        } else {
                            tvUserName.setText("User: Customer");
                        }
                    });
            } else {
                tvUserName.setText("User: Customer");
            }
        } else {
            tvUserName.setText("User: " + name);
        }

        tvDroneId.setText("Drone ID: " + (booking.getDroneId() != null ? booking.getDroneId() : "Pending Assignment"));
        tvSector.setText("Sector: " + (booking.getSector() != null ? booking.getSector() : "N/A"));
        tvTimeRange.setText("Time: " + (booking.getStartTime() != null ? booking.getStartTime() : "??") + " - " + (booking.getEndTime() != null ? booking.getEndTime() : "??"));
        tvTotalCost.setText("₹" + booking.getTotalAmount());
        tvAdvancePaid.setText("₹" + booking.getAdvanceAmount());
        tvRemainingAmount.setText("₹" + booking.getRemainingAmount());
    }
}
