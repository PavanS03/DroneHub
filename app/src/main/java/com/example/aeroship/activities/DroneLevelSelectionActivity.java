package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.DronePackage;
import com.example.aeroship.utils.DroneDataProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DroneLevelSelectionActivity extends AppCompatActivity {

    private String sectorName;
    private TextView tvSectorTitle, tvEmptyState;
    private RecyclerView rvDrones;
    private ProgressBar progressBar;
    private DatabaseReference levelsRef;
    private LevelAdapter adapter;
    private List<LevelData> levelList;

    private static class LevelData {
        String levelName;
        long count;
        long max;
        DronePackage packageInfo;

        LevelData(String levelName, long count, long max, DronePackage packageInfo) {
            this.levelName = levelName;
            this.count = count;
            this.max = max;
            this.packageInfo = packageInfo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_level_selection);

        sectorName = getIntent().getStringExtra("sector_name");
        
        if (sectorName == null) {
            Toast.makeText(this, "Sector not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        levelsRef = FirebaseDatabase.getInstance().getReference("SECTORS")
                .child(sectorName).child("levels");

        loadLevelsFromFirebase();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvSectorTitle = findViewById(R.id.tvSectorTitle);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvDrones = findViewById(R.id.rvDrones);
        progressBar = findViewById(R.id.progressBar);

        tvSectorTitle.setText(sectorName + " Available Levels");
    }

    private void setupRecyclerView() {
        levelList = new ArrayList<>();
        adapter = new LevelAdapter(levelList);
        rvDrones.setLayoutManager(new LinearLayoutManager(this));
        rvDrones.setAdapter(adapter);
    }

    private void loadLevelsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Map UI sector name to DataProvider key
        String providerKey = sectorName;
        if (sectorName.equalsIgnoreCase("Agriculture")) providerKey = "Farming";
        else if (sectorName.equalsIgnoreCase("Emergency")) providerKey = "Surveillance";
        else if (sectorName.equalsIgnoreCase("Defense")) providerKey = "Surveillance";
        else if (sectorName.equalsIgnoreCase("Mapping")) providerKey = "Construction";
        
        final Map<String, DronePackage> sectorPackages = DroneDataProvider.getDroneData().get(providerKey);

        levelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                levelList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot levelSnap : snapshot.getChildren()) {
                        String levelName = levelSnap.getKey();
                        Boolean enabled = levelSnap.child("enabled").getValue(Boolean.class);
                        Long count = levelSnap.child("count").getValue(Long.class);
                        Long max = levelSnap.child("max").getValue(Long.class);

                        // Match "Medium" (DB) to "Mid" (Provider)
                        String lookupKey = levelName;
                        if ("Medium".equalsIgnoreCase(levelName)) lookupKey = "Mid";

                        if (enabled != null && enabled && count != null) {
                            DronePackage pkg = (sectorPackages != null) ? sectorPackages.get(lookupKey) : null;
                            levelList.add(new LevelData(levelName, count, max != null ? max : 0, pkg));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(levelList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DroneLevelSelectionActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.ViewHolder> {
        private List<LevelData> list;

        public LevelAdapter(List<LevelData> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drone_level_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LevelData item = list.get(position);
            
            holder.tvLevelTitle.setText(item.levelName.toUpperCase() + " LEVEL");
            
            if (item.packageInfo != null) {
                holder.tvFeatures.setText(item.packageInfo.getFeatures());
                holder.tvPrice.setText("Available Drones: " + item.count + " / " + item.max);
            } else {
                holder.tvFeatures.setText("Standard " + item.levelName + " grade equipment.\nReliable and efficient.");
                holder.tvPrice.setText("Available Drones: " + item.count + " / " + item.max);
            }

            holder.itemView.setAlpha(item.count > 0 ? 1.0f : 0.5f);
            holder.itemView.setOnClickListener(v -> {
                if (item.count <= 0) {
                    Toast.makeText(DroneLevelSelectionActivity.this, "No drones available in this level", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(DroneLevelSelectionActivity.this, PlaceOrderActivity.class);
                intent.putExtra("sector", sectorName);
                intent.putExtra("level", item.levelName);
                int price = (item.packageInfo != null) ? item.packageInfo.getPricePerHour() : 2500;
                intent.putExtra("pricePerHour", price);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLevelTitle, tvFeatures, tvPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLevelTitle = itemView.findViewById(R.id.tvLevelTitle);
                tvFeatures = itemView.findViewById(R.id.tvFeatures);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }
    }
}
