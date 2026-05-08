package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperatorMissionsActivity extends AppCompatActivity {

    private String operatorId, operatorName;
    private TextView tvOperatorName, tvMissionCount, tvEarnings;
    private ImageView btnBack;
    private RecyclerView recyclerMissions;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private List<Booking> missionList = new ArrayList<>();
    private MissionAdapter adapter;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_missions);

        operatorId = getIntent().getStringExtra("operatorId");
        operatorName = getIntent().getStringExtra("operatorName");

        if (operatorId == null) {
            Toast.makeText(this, "Error: Operator ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvOperatorName = findViewById(R.id.tvOperatorName);
        tvMissionCount = findViewById(R.id.tvMissionCount);
        tvEarnings = findViewById(R.id.tvEarnings);
        btnBack = findViewById(R.id.btnBack);
        recyclerMissions = findViewById(R.id.recyclerMissions);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        tvOperatorName.setText(operatorName != null ? operatorName + "'s Missions" : "Operator Missions");

        recyclerMissions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MissionAdapter(this, missionList, new MissionAdapter.OnMissionClickListener() {
            @Override
            public void onStart(Booking booking) {}
            @Override
            public void onEnd(Booking booking) {}
            @Override
            public void onShowReceipt(Booking booking) {}
        });
        recyclerMissions.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference("ORDERS");

        loadMissions();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadMissions() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Query query = reference.orderByChild("assignedOperatorId").equalTo(operatorId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                missionList.clear();
                int totalEarnings = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking booking = ds.getValue(Booking.class);
                    if (booking != null) {
                        missionList.add(booking);
                        if ("Completed".equalsIgnoreCase(booking.getStatus())) {
                            // Assuming operator gets 10% of total amount as mission earnings
                            totalEarnings += (booking.getTotalAmount() * 0.1);
                        }
                    }
                }

                Collections.sort(missionList, (b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                tvMissionCount.setText("Total: " + missionList.size());
                tvEarnings.setText("Earnings: ₹" + totalEarnings);

                if (missionList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OperatorMissionsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
