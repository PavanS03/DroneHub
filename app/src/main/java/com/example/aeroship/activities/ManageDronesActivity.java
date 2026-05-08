package com.example.aeroship.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.DroneAdapter;
import com.example.aeroship.models.DronePackage;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageDronesActivity extends AppCompatActivity
        implements DroneAdapter.DroneActionListener {

    private RecyclerView recyclerDrones;
    private MaterialButton btnAddDrone;
    private TextView tvEmpty;
    private SearchView searchDrones;

    private final List<DronePackage> droneList = new ArrayList<>();
    private final List<DronePackage> fullList = new ArrayList<>();
    private DroneAdapter adapter;

    private DatabaseReference reference;

    private String sectorId = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drones);

        recyclerDrones = findViewById(R.id.recyclerDrones);
        btnAddDrone = findViewById(R.id.btnAddDrone);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchDrones = findViewById(R.id.searchDrones);

        recyclerDrones.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null && getIntent().hasExtra("sectorId")) {
            sectorId = getIntent().getStringExtra("sectorId");
        }

        if (sectorId == null || sectorId.trim().isEmpty()) {
            sectorId = "default";
        }

        adapter = new DroneAdapter(droneList, this); 
        recyclerDrones.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance()
                .getReference("DRONES")
                .child(sectorId);

        setupSearch();
        loadDrones();

        btnAddDrone.setOnClickListener(v -> showAddDialog());
    }

    private void setupSearch() {
        searchDrones.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void filter(String query) {
        List<DronePackage> filteredList = new ArrayList<>();
        String searchText = query.toLowerCase().trim();

        for (DronePackage item : fullList) {
            if (item.getLevel().toLowerCase().contains(searchText)) {
                filteredList.add(item);
            }
        }

        droneList.clear();
        droneList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (droneList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerDrones.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerDrones.setVisibility(View.VISIBLE);
        }
    }

    private void loadDrones() {

        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                fullList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    DronePackage drone = ds.getValue(DronePackage.class);

                    if (drone != null) {
                        fullList.add(drone);
                    }
                }

                filter(searchDrones.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDronesActivity.this,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {

        EditText input = new EditText(this);
        input.setHint("Level (Low / Medium / High)");

        new AlertDialog.Builder(this)
                .setTitle("Add Drone")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {

                    String level = input.getText().toString().trim();

                    if (TextUtils.isEmpty(level)) return;

                    String id = UUID.randomUUID().toString();

                    DronePackage drone = new DronePackage(
                            id, sectorId, level, 5, 1000, true
                    );

                    reference.child(id).setValue(drone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(DronePackage drone) {

        EditText input = new EditText(this);
        input.setText(drone.getLevel());

        new AlertDialog.Builder(this)
                .setTitle("Edit Drone")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {

                    String newLevel = input.getText().toString().trim();

                    if (TextUtils.isEmpty(newLevel)) return;

                    reference.child(drone.getId())
                            .child("level")
                            .setValue(newLevel);

                    Toast.makeText(this, "Updated ✅", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(DronePackage drone) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Drone")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> {

                    reference.child(drone.getId()).removeValue();

                    Toast.makeText(this, "Deleted ❌", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggle(DronePackage drone) {

        boolean newStatus = !drone.isActive();

        reference.child(drone.getId())
                .child("active")
                .setValue(newStatus);

        Toast.makeText(this,
                newStatus ? "Activated ✅" : "Deactivated ❌",
                Toast.LENGTH_SHORT).show();
    }
}