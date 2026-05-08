package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminSectorLevelSelectionActivity extends AppCompatActivity {

    private String sectorName;
    private TextView tvSectorTitle;
    private Switch switchLow, switchMedium, switchHigh;
    private TextInputEditText etLowCount, etMediumCount, etHighCount;
    private MaterialButton btnSave;
    private DatabaseReference levelsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sector_level_selection);

        sectorName = getIntent().getStringExtra("sector_name");
        if (sectorName == null) {
            Toast.makeText(this, "Sector name missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        levelsRef = FirebaseDatabase.getInstance().getReference("SECTORS")
                .child(sectorName).child("levels");

        loadLevelData();

        btnSave.setOnClickListener(v -> saveLevelData());
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvSectorTitle = findViewById(R.id.tvSectorTitle);
        tvSectorTitle.setText("Sector: " + sectorName);

        switchLow = findViewById(R.id.switchLow);
        switchMedium = findViewById(R.id.switchMedium);
        switchHigh = findViewById(R.id.switchHigh);

        etLowCount = findViewById(R.id.etLowCount);
        etMediumCount = findViewById(R.id.etMediumCount);
        etHighCount = findViewById(R.id.etHighCount);

        btnSave = findViewById(R.id.btnSaveAllLevels);
    }

    private void loadLevelData() {
        levelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("Low")) {
                    updateLevelUI(snapshot.child("Low"), switchLow, etLowCount);
                    updateLevelUI(snapshot.child("Medium"), switchMedium, etMediumCount);
                    updateLevelUI(snapshot.child("High"), switchHigh, etHighCount);
                } else {
                    Map<String, Object> defaults = new HashMap<>();
                    defaults.put("Low", createLevelMap(4));
                    defaults.put("Medium", createLevelMap(3));
                    defaults.put("High", createLevelMap(3));
                    levelsRef.updateChildren(defaults);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminSectorLevelSelectionActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Map<String, Object> createLevelMap(int count) {
        Map<String, Object> level = new HashMap<>();
        level.put("enabled", true);
        level.put("count", (long) count);
        return level;
    }

    private void updateLevelUI(DataSnapshot levelSnap, Switch sw, TextInputEditText et) {
        Boolean enabled = levelSnap.child("enabled").getValue(Boolean.class);
        Long count = levelSnap.child("count").getValue(Long.class);

        if (enabled != null) sw.setChecked(enabled);
        if (count != null) et.setText(String.valueOf(count));
    }

    private void saveLevelData() {
        Map<String, Object> updates = new HashMap<>();
        
        updates.put("Low/enabled", switchLow.isChecked());
        long lowCount = getLongFromEditText(etLowCount);
        updates.put("Low/count", lowCount);
        updates.put("Low/max", lowCount);
        
        updates.put("Medium/enabled", switchMedium.isChecked());
        long mediumCount = getLongFromEditText(etMediumCount);
        updates.put("Medium/count", mediumCount);
        updates.put("Medium/max", mediumCount);
        
        updates.put("High/enabled", switchHigh.isChecked());
        long highCount = getLongFromEditText(etHighCount);
        updates.put("High/count", highCount);
        updates.put("High/max", highCount);

        levelsRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Levels updated successfully", Toast.LENGTH_SHORT).show();
            
            long total = lowCount + mediumCount + highCount;
            DatabaseReference sectorRef = FirebaseDatabase.getInstance().getReference("SECTORS").child(sectorName);
            sectorRef.child("droneCount").setValue(total);
            sectorRef.child("maxDrones").setValue(total);
            
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update levels", Toast.LENGTH_SHORT).show();
        });
    }

    private long getLongFromEditText(TextInputEditText et) {
        String val = et.getText().toString().trim();
        if (val.isEmpty()) return 0;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
