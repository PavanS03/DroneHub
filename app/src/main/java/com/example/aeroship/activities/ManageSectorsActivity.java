package com.example.aeroship.activities;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ManageSectorsActivity extends AppCompatActivity {

    private DatabaseReference sectorsRef;
    private List<Pair<DatabaseReference, ValueEventListener>> listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sectors);

        startBackgroundAnimation();

        sectorsRef = FirebaseDatabase.getInstance().getReference("SECTORS");

        setupSector(R.id.cardAgriculture, R.id.switchAgriculture, R.id.etAgricultureCount, R.id.btnSaveAgriculture, "Agriculture");
        setupSector(R.id.cardDelivery, R.id.switchDelivery, R.id.etDeliveryCount, R.id.btnSaveDelivery, "Delivery");
        setupSector(R.id.cardSurveillance, R.id.switchSurveillance, R.id.etSurveillanceCount, R.id.btnSaveSurveillance, "Surveillance");
        setupSector(R.id.cardEmergency, R.id.switchEmergency, R.id.etEmergencyCount, R.id.btnSaveEmergency, "Emergency");
        setupSector(R.id.cardWedding, R.id.switchWedding, R.id.etWeddingCount, R.id.btnSaveWedding, "Wedding");
        setupSector(R.id.cardConstruction, R.id.switchConstruction, R.id.etConstructionCount, R.id.btnSaveConstruction, "Construction");
        setupSector(R.id.cardDefense, R.id.switchDefense, R.id.etDefenseCount, R.id.btnSaveDefense, "Defense");
        setupSector(R.id.cardMapping, R.id.switchMapping, R.id.etMappingCount, R.id.btnSaveMapping, "Mapping");
    }

    private void startBackgroundAnimation() {

        View root = findViewById(android.R.id.content);

        if (root != null && root.getBackground() instanceof AnimationDrawable) {

            AnimationDrawable animationDrawable = (AnimationDrawable) root.getBackground();

            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(2000);

            animationDrawable.start();
        }
    }

    private void setupSector(int cardId, int switchId, int editTextId, int buttonId, String sectorName) {

        View card = findViewById(cardId);
        Switch sectorSwitch = findViewById(switchId);
        EditText etCount = findViewById(editTextId);
        MaterialButton btnSave = findViewById(buttonId);

        DatabaseReference sectorRef = sectorsRef.child(sectorName);

        ValueEventListener listener = sectorRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Boolean enabled = snapshot.child("enabled").getValue(Boolean.class);

                Long maxDrones = snapshot.child("maxDrones").getValue(Long.class);

                if (maxDrones == null) {
                    maxDrones = snapshot.child("droneCount").getValue(Long.class);
                }

                if (enabled != null) {
                    sectorSwitch.setChecked(enabled);
                }

                if (maxDrones != null) {
                    etCount.setText(String.valueOf(maxDrones));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
        
        listeners.add(new Pair<>(sectorRef, listener));

        sectorSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                sectorRef.child("enabled").setValue(isChecked)
        );

        View.OnClickListener openLevels = v -> {
            android.content.Intent intent = new android.content.Intent(ManageSectorsActivity.this, AdminSectorLevelSelectionActivity.class);
            intent.putExtra("sector_name", sectorName);
            startActivity(intent);
        };

        btnSave.setOnClickListener(openLevels);
        if (card != null) {
            card.setOnClickListener(openLevels);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Pair<DatabaseReference, ValueEventListener> pair : listeners) {
            pair.first.removeEventListener(pair.second);
        }
    }
}