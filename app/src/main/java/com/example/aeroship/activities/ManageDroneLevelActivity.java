package com.example.aeroship.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.DroneItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDroneLevelActivity extends AppCompatActivity {

    private String sectorId;
    private TextView tvSectorTitle, tvTotalDrones, tvEmptyState;
    private RecyclerView rvDrones;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddDrone;
    private DatabaseReference dronesRef;
    private DroneAdapter adapter;
    private List<DroneItem> droneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drone_level);

        sectorId = getIntent().getStringExtra("sector_name");
        if (sectorId == null) {
            Toast.makeText(this, "Sector ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        dronesRef = FirebaseDatabase.getInstance().getReference("SECTORS")
                .child(sectorId).child("drones");

        loadDrones();

        fabAddDrone.setOnClickListener(v -> showAddDroneDialog());
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvSectorTitle = findViewById(R.id.tvSectorTitle);
        tvTotalDrones = findViewById(R.id.tvTotalDrones);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvDrones = findViewById(R.id.rvDrones);
        progressBar = findViewById(R.id.progressBar);
        fabAddDrone = findViewById(R.id.fabAddDrone);

        tvSectorTitle.setText("Sector: " + sectorId);
    }

    private void setupRecyclerView() {
        droneList = new ArrayList<>();
        adapter = new DroneAdapter(droneList);
        rvDrones.setLayoutManager(new LinearLayoutManager(this));
        rvDrones.setAdapter(adapter);
    }

    private void loadDrones() {
        progressBar.setVisibility(View.VISIBLE);
        dronesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                droneList.clear();
                int totalCount = 0;
                for (DataSnapshot droneSnap : snapshot.getChildren()) {
                    DroneItem drone = droneSnap.getValue(DroneItem.class);
                    if (drone != null) {
                        droneList.add(drone);
                        totalCount += drone.getCount();
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                tvTotalDrones.setText("Total Drones in Sector: " + totalCount);
                tvEmptyState.setVisibility(droneList.isEmpty() ? View.VISIBLE : View.GONE);
                
                // Update the top-level droneCount and maxDrones for the sector
                DatabaseReference sectorRef = FirebaseDatabase.getInstance().getReference("SECTORS").child(sectorId);
                sectorRef.child("droneCount").setValue(totalCount);
                sectorRef.child("maxDrones").setValue(totalCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDroneLevelActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDroneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_drone, null);
        builder.setView(view);

        TextInputEditText etDroneId = view.findViewById(R.id.etDroneId);
        TextInputEditText etDroneCount = view.findViewById(R.id.etDroneCount);
        View btnSubmit = view.findViewById(R.id.btnSubmitDrone);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            String id = etDroneId.getText().toString().trim();
            String countStr = etDroneCount.getText().toString().trim();

            if (id.isEmpty()) {
                etDroneId.setError("Required");
                return;
            }
            if (countStr.isEmpty()) {
                etDroneCount.setError("Required");
                return;
            }

            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                etDroneCount.setError("Must be > 0");
                return;
            }

            addDroneToFirebase(id, count, dialog);
        });

        dialog.show();
    }

    private void addDroneToFirebase(String id, int count, AlertDialog dialog) {
        DroneItem drone = new DroneItem(id, count);
        dronesRef.child(id).setValue(drone).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Drone added successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to add drone", Toast.LENGTH_SHORT).show();
        });
    }

    private class DroneAdapter extends RecyclerView.Adapter<DroneAdapter.ViewHolder> {
        private List<DroneItem> list;

        public DroneAdapter(List<DroneItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drone_manage, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DroneItem item = list.get(position);
            holder.tvId.setText(item.getDroneId());
            holder.tvCount.setText("Count: " + item.getCount());
            holder.btnDelete.setOnClickListener(v -> {
                dronesRef.child(item.getDroneId()).removeValue();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvCount;
            ImageView ivIcon;
            View btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvDroneId);
                tvCount = itemView.findViewById(R.id.tvDroneCount);
                ivIcon = itemView.findViewById(R.id.ivDroneIcon);
                btnDelete = itemView.findViewById(R.id.btnDeleteDrone);
            }
        }
    }
}