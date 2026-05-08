package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aeroship.R;
import com.example.aeroship.adapters.MissionAdapter;
import com.example.aeroship.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OperatorDashboardActivity extends AppCompatActivity {

    private RecyclerView rvMissions;
    private MissionAdapter adapter;
    private List<Booking> missionList;
    private DatabaseReference ordersRef, dronesRef;
    private ValueEventListener missionsListener;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private TextView tvActiveMissions, tvCompletedMissions, tvEmpty, tvGreeting;
    private View btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        initViews();
        setupRecyclerView();
        setupSearch();
        loadMissions();
        setupLogout();
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        dronesRef = FirebaseDatabase.getInstance().getReference("DRONES");
        
        rvMissions = findViewById(R.id.rvMissions);
        progressBar = findViewById(R.id.progressBar);
        tvActiveMissions = findViewById(R.id.tvActiveMissions);
        tvCompletedMissions = findViewById(R.id.tvCompletedMissions);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);

        loadOperatorName();
    }

    private void loadOperatorName() {
        if (auth.getCurrentUser() == null) return;
        FirebaseDatabase.getInstance().getReference("USERS")
                .child(auth.getCurrentUser().getUid())
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) tvGreeting.setText("Hello, " + name);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupLogout() {
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            auth.signOut();
                            Intent intent = new Intent(OperatorDashboardActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }
    }

    private void setupRecyclerView() {
        missionList = new ArrayList<>();
        adapter = new MissionAdapter(this, missionList, new MissionAdapter.OnMissionClickListener() {
            @Override
            public void onStart(Booking booking) {
                startMission(booking);
            }

            @Override
            public void onEnd(Booking booking) {
                endMission(booking);
            }

            @Override
            public void onShowReceipt(Booking booking) {
                Intent intent = new Intent(OperatorDashboardActivity.this, ReceiptActivity.class);
                intent.putExtra("bookingId", booking.getBookingId());
                startActivity(intent);
            }
        });
        rvMissions.setLayoutManager(new LinearLayoutManager(this));
        rvMissions.setAdapter(adapter);
    }

    private List<Booking> fullList = new java.util.ArrayList<>();

    private void loadMissions() {
        if (auth.getCurrentUser() == null) return;
        String operatorId = auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        missionsListener = ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                missionList.clear();
                fullList.clear();
                int activeCount = 0;
                int completedCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking booking = ds.getValue(Booking.class);
                    if (booking != null && operatorId.equals(booking.getAssignedOperatorId())) {
                        missionList.add(booking);
                        fullList.add(booking);
                        if (booking.getStatus().equalsIgnoreCase("Completed")) {
                            completedCount++;
                        } else {
                            activeCount++;
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                
                tvActiveMissions.setText(String.valueOf(activeCount));
                tvCompletedMissions.setText(String.valueOf(completedCount));
                
                tvEmpty.setVisibility(missionList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OperatorDashboardActivity.this, "Failed to load missions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        List<Booking> filteredList = new java.util.ArrayList<>();
        for (Booking item : fullList) {
            boolean matches = false;
            if (item.getUserName() != null && item.getUserName().toLowerCase().contains(text.toLowerCase())) matches = true;
            if (item.getSector() != null && item.getSector().toLowerCase().contains(text.toLowerCase())) matches = true;
            if (item.getStatus() != null && item.getStatus().toLowerCase().contains(text.toLowerCase())) matches = true;
            
            if (matches) filteredList.add(item);
        }
        missionList.clear();
        missionList.addAll(filteredList);
        adapter.notifyDataSetChanged();
        
        tvEmpty.setVisibility(missionList.isEmpty() ? View.VISIBLE : View.GONE);
        if (missionList.isEmpty() && !text.isEmpty()) {
            tvEmpty.setText("No matching missions found.");
        } else if (missionList.isEmpty()) {
            tvEmpty.setText("No missions assigned yet.");
        }
    }

    private void startMission(Booking booking) {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "In Progress");
        updates.put("pickupTime", currentTime);
        
        ordersRef.child(booking.getBookingId()).updateChildren(updates).addOnSuccessListener(aVoid -> {
            if (booking.getDroneId() != null) {
                dronesRef.child(booking.getDroneId()).child("status").setValue("Busy");
            }
            Toast.makeText(this, "Mission Started!", Toast.LENGTH_SHORT).show();
        });
    }

    private void endMission(Booking booking) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("End Mission");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter Flight Time in Minutes");
        builder.setView(input);

        builder.setPositiveButton("Calculate & Complete", (dialog, which) -> {
            String timeStr = input.getText().toString();
            if (timeStr.isEmpty()) {
                Toast.makeText(this, "Please enter flight time", Toast.LENGTH_SHORT).show();
                return;
            }

            int flightTime = Integer.parseInt(timeStr);
            int totalFare = flightTime * 10;
            int remainingAmount = totalFare - booking.getAdvanceAmount();
            if (remainingAmount < 0) remainingAmount = 0;

            String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "Completed");
            updates.put("returnTime", currentTime);
            updates.put("flightTime", flightTime);
            updates.put("totalAmount", totalFare);
            updates.put("remainingAmount", remainingAmount);
            
            ordersRef.child(booking.getBookingId()).updateChildren(updates).addOnSuccessListener(aVoid -> {
                if (booking.getDroneId() != null) {
                    dronesRef.child(booking.getDroneId()).child("status").setValue("Available");
                }
                Toast.makeText(this, "Mission Completed! Total: ₹" + totalFare, Toast.LENGTH_LONG).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && missionsListener != null) {
            ordersRef.removeEventListener(missionsListener);
        }
    }
}