package com.example.aeroship.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.DroneAdapter;
import com.example.aeroship.models.DronePackage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageDronesActivity extends AppCompatActivity {

    private RecyclerView rvDrones;
    private DroneAdapter adapter;
    private List<DronePackage> droneList;
    private List<DronePackage> fullList = new ArrayList<>();
    private EditText etSearchDrone;
    private ChipGroup chipGroupSector;
    private DatabaseReference dronesRef;
    private ProgressBar progressBar;
    private MaterialButton btnAddDrone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_drones);

        initViews();
        setupRecyclerView();
        loadDrones();

        btnAddDrone.setOnClickListener(v -> showAddEditDroneDialog(null));
    }

    private void initViews() {
        rvDrones = findViewById(R.id.rvDrones);
        progressBar = findViewById(R.id.progressBar);
        btnAddDrone = findViewById(R.id.btnAddDrone);
        etSearchDrone = findViewById(R.id.etSearchDrone);
        chipGroupSector = findViewById(R.id.chipGroupSector);

        dronesRef = FirebaseDatabase.getInstance().getReference("DRONES");

        setupSearchAndFilter();
    }

    private void setupSearchAndFilter() {
        etSearchDrone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDrones();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupSector.setOnCheckedStateChangeListener((group, checkedIds) -> filterDrones());
    }

    private void filterDrones() {
        String query = etSearchDrone.getText().toString().toLowerCase().trim();
        int checkedChipId = chipGroupSector.getCheckedChipId();
        String sectorFilter = "";

        if (checkedChipId == R.id.chipAgriculture) sectorFilter = "Agriculture";
        else if (checkedChipId == R.id.chipMedical) sectorFilter = "Medical";
        else if (checkedChipId == R.id.chipDelivery) sectorFilter = "Delivery";

        droneList.clear();
        for (DronePackage drone : fullList) {
            String droneName = drone.getPackageName() != null ? drone.getPackageName() : drone.getPackageId();
            if (droneName == null) droneName = "";
            
            boolean matchesQuery = droneName.toLowerCase().contains(query);
            boolean matchesSector = sectorFilter.isEmpty() || (drone.getSectorId() != null && drone.getSectorId().equalsIgnoreCase(sectorFilter));

            if (matchesQuery && matchesSector) {
                droneList.add(drone);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        droneList = new ArrayList<>();
        adapter = new DroneAdapter(droneList, new DroneAdapter.DroneActionListener() {
            @Override
            public void onEdit(DronePackage drone) {
                showAddEditDroneDialog(drone);
            }

            @Override
            public void onDelete(DronePackage drone) {
                confirmDelete(drone);
            }

            @Override
            public void onToggle(DronePackage drone) {
                toggleDroneStatus(drone);
            }
        });
        rvDrones.setLayoutManager(new LinearLayoutManager(this));
        rvDrones.setAdapter(adapter);
    }

    private void toggleDroneStatus(DronePackage drone) {
        boolean newStatus = !drone.isAvailable();
        dronesRef.child(drone.getSectorId()).child(drone.getPackageId()).child("available").setValue(newStatus);
    }

    private void loadDrones() {
        progressBar.setVisibility(View.VISIBLE);
        dronesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot sectorSnap : snapshot.getChildren()) {
                    for (DataSnapshot droneSnap : sectorSnap.getChildren()) {
                        DronePackage drone = droneSnap.getValue(DronePackage.class);
                        if (drone != null) {
                            fullList.add(drone);
                        }
                    }
                }
                filterDrones();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminManageDronesActivity.this, "Failed to load drones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDroneDialog(DronePackage drone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_drone, null);
        builder.setView(view);

        EditText etId = view.findViewById(R.id.etDroneId);
        Spinner spSector = view.findViewById(R.id.spSector);
        Spinner spLevel = view.findViewById(R.id.spLevel);
        EditText etRate = view.findViewById(R.id.etRate);
        Spinner spStatus = view.findViewById(R.id.spStatus);
        Button btnSave = view.findViewById(R.id.btnSave);


        String[] sectors = {"Agriculture", "Logistics", "Photography", "Surveillance", "Medical", "Delivery"};
        String[] levels = {"Low", "Medium", "High"};
        String[] statusList = {"Available", "Busy", "Maintenance"};

        spSector.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sectors));
        spLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levels));
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusList));

        boolean isEdit = drone != null;
        if (isEdit) {
            etId.setText(drone.getPackageId());
            etId.setEnabled(false); // ID is unique key
            etRate.setText(String.valueOf(drone.getPricePerHour()));
            setSpinnerSelection(spSector, sectors, drone.getSectorId());
            setSpinnerSelection(spLevel, levels, drone.getLevel());
        }

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String sector = spSector.getSelectedItem().toString();
            String level = spLevel.getSelectedItem().toString();
            String rateStr = etRate.getText().toString().trim();

            if (TextUtils.isEmpty(id) || TextUtils.isEmpty(rateStr)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int rate = Integer.parseInt(rateStr);
            DronePackage newDrone = new DronePackage(id, sector, level, 10, rate, true);

            dronesRef.child(sector).child(id).setValue(newDrone).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Drone Saved", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void setSpinnerSelection(Spinner spinner, String[] list, String value) {
        if (value == null) return;
        for (int i = 0; i < list.length; i++) {
            if (list[i].equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void confirmDelete(DronePackage drone) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Drone")
                .setMessage("Delete drone " + drone.getPackageId() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dronesRef.child(drone.getSectorId()).child(drone.getPackageId()).removeValue();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}