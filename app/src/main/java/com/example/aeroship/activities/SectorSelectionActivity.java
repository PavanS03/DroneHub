package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.aeroship.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SectorSelectionActivity extends AppCompatActivity {

    private ViewGroup sectorContainer;
    private DatabaseReference sectorsRef;
    private ValueEventListener sectorListener;

    private TextView tvDroneAgriculture, tvDroneDelivery, tvDroneSurveillance,
            tvDroneEmergency, tvDroneWedding, tvDroneDroneConstruction,
            tvDroneDefense, tvDroneMapping;

    private int agriculturePrice, deliveryPrice, surveillancePrice,
            emergencyPrice, weddingPrice, constructionPrice,
            defensePrice, mappingPrice;

    private int agricultureCount = 0;
    private int deliveryCount = 0;
    private int surveillanceCount = 0;
    private int emergencyCount = 0;
    private int weddingCount = 0;
    private int constructionCount = 0;
    private int defenseCount = 0;
    private int mappingCount = 0;

    private int agricultureMax = 10;
    private int deliveryMax = 10;
    private int surveillanceMax = 10;
    private int emergencyMax = 10;
    private int weddingMax = 10;
    private int constructionMax = 10;
    private int defenseMax = 10;
    private int mappingMax = 10;

    private List<SectorViewItem> allSectors = new ArrayList<>();
    private SearchView searchSectors;
    private TextView tvNoResults;

    private static class SectorViewItem {
        View card;
        String name;

        SectorViewItem(View card, String name) {
            this.card = card;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sector_selection);

        sectorsRef = FirebaseDatabase.getInstance().getReference("SECTORS");

        initViews();
        loadDroneCountsRealtime();

        ViewTreeObserver vto = getWindow().getDecorView().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {

            ViewGroup root = findViewById(android.R.id.content);
            View scrollView = root.getChildAt(0);

            if (scrollView instanceof ViewGroup) {
                sectorContainer = (ViewGroup) ((ViewGroup) scrollView).getChildAt(0);
            }

            if (sectorContainer != null) {
                populateSectorList();
                setupSectorClickListeners();
                animateCards();
            }
        });
    }

    private void initViews() {
        tvDroneAgriculture = findViewById(R.id.tvDroneAgriculture);
        tvDroneDelivery = findViewById(R.id.tvDroneDelivery);
        tvDroneSurveillance = findViewById(R.id.tvDroneSurveillance);
        tvDroneEmergency = findViewById(R.id.tvDroneEmergency);
        tvDroneWedding = findViewById(R.id.tvDroneWedding);
        tvDroneDroneConstruction = findViewById(R.id.tvDroneConstruction);
        tvDroneDefense = findViewById(R.id.tvDroneDefense);
        tvDroneMapping = findViewById(R.id.tvDroneMapping);
        
        searchSectors = findViewById(R.id.searchSectors);
        tvNoResults = findViewById(R.id.tvNoResults);

        setupSearch();
    }

    private void setupSearch() {
        searchSectors.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSectors(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSectors(newText);
                return true;
            }
        });
    }

    private void filterSectors(String query) {
        if (allSectors.isEmpty() && sectorContainer != null) {
            populateSectorList();
        }

        boolean found = false;
        String lowerQuery = query.toLowerCase(Locale.ROOT).trim();

        for (SectorViewItem item : allSectors) {
            boolean matches = TextUtils.isEmpty(lowerQuery) || item.name.toLowerCase(Locale.ROOT).contains(lowerQuery);
            item.card.setVisibility(matches ? View.VISIBLE : View.GONE);
            if (matches) found = true;
        }

        tvNoResults.setVisibility(found || TextUtils.isEmpty(query) ? View.GONE : View.VISIBLE);
    }

    private void populateSectorList() {
        allSectors.clear();
        if (sectorContainer == null) return;
        
        for (int i = 0; i < sectorContainer.getChildCount(); i++) {
            View child = sectorContainer.getChildAt(i);
            if (child instanceof com.google.android.material.card.MaterialCardView) {
                String name = getSectorName(allSectors.size());
                allSectors.add(new SectorViewItem(child, name));
            }
        }
    }


    private void loadDroneCountsRealtime() {

        sectorListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() < 8) {
                    initializeSectors();
                    return;
                }

                agricultureCount = updateSector(snapshot, "Agriculture", tvDroneAgriculture);
                deliveryCount = updateSector(snapshot, "Delivery", tvDroneDelivery);
                surveillanceCount = updateSector(snapshot, "Surveillance", tvDroneSurveillance);
                emergencyCount = updateSector(snapshot, "Emergency", tvDroneEmergency);
                weddingCount = updateSector(snapshot, "Wedding", tvDroneWedding);
                constructionCount = updateSector(snapshot, "Construction", tvDroneDroneConstruction);
                defenseCount = updateSector(snapshot, "Defense", tvDroneDefense);
                mappingCount = updateSector(snapshot, "Mapping", tvDroneMapping);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SectorSelectionActivity.this,
                        "Failed to load data",
                        Toast.LENGTH_SHORT).show();
            }
        };

        sectorsRef.addValueEventListener(sectorListener);
    }

    private void initializeSectors() {
        String[] sectors = {"Agriculture", "Delivery", "Surveillance", "Emergency", "Wedding", "Construction", "Defense", "Mapping"};
        int[] prices = {1500, 1200, 2500, 3000, 5000, 4500, 8000, 3500};

        for (int i = 0; i < sectors.length; i++) {
            java.util.HashMap<String, Object> map = new java.util.HashMap<>();
            map.put("name", sectors[i]);
            map.put("droneCount", 10L); 
            map.put("maxDrones", 10L);
            map.put("enabled", true);
            map.put("pricePerHour", (long) prices[i]);

            // Add Default Levels (divided for 10 total)
            java.util.HashMap<String, Object> levels = new java.util.HashMap<>();
            levels.put("Low", createLevelMap(4));
            levels.put("Medium", createLevelMap(3));
            levels.put("High", createLevelMap(3));
            map.put("levels", levels);

            sectorsRef.child(sectors[i]).setValue(map);
        }
    }

    private java.util.HashMap<String, Object> createLevelMap(int count) {
        java.util.HashMap<String, Object> level = new java.util.HashMap<>();
        level.put("enabled", true);
        level.put("count", (long) count);
        level.put("max", (long) count);
        return level;
    }

    private int updateSector(DataSnapshot snapshot, String sector, TextView tv) {
        if (tv == null) return 0;

        DataSnapshot sectorSnap = snapshot.child(sector);
        Long price = sectorSnap.child("pricePerHour").getValue(Long.class);
        int priceValue = price != null ? price.intValue() : 500;

        if (!sectorSnap.exists()) {
            initializeLevelsForSector(sector, priceValue);
            tv.setText("Initializing...");
            return 10;
        }

        long calculatedTotalCount = 0;
        long calculatedMaxTotal = 0;
        
        DataSnapshot levelsSnap = sectorSnap.child("levels");
        if (levelsSnap.exists()) {
            for (DataSnapshot levelSnap : levelsSnap.getChildren()) {
                Boolean enabled = levelSnap.child("enabled").getValue(Boolean.class);
                Long count = levelSnap.child("count").getValue(Long.class);
                Long max = levelSnap.child("max").getValue(Long.class);
                
                if (enabled != null && enabled && count != null) {
                    calculatedTotalCount += count;
                }
                
                if (max != null) {
                    calculatedMaxTotal += max;
                }
            }
            
            if (calculatedMaxTotal < 10) {
                calculatedMaxTotal = 10;
            }
        } else {
            initializeLevelsForSector(sector, priceValue);
            calculatedTotalCount = 10;
            calculatedMaxTotal = 10;
        }

        Boolean enabled = sectorSnap.child("enabled").getValue(Boolean.class);
        if (enabled == null) enabled = true;

        savePrice(sector, priceValue);
        saveMax(sector, (int) calculatedMaxTotal);

        if (!enabled) {
            tv.setText("Service Disabled ❌");
            tv.setTextColor(android.graphics.Color.GRAY);
            return 0;
        }

        if (calculatedTotalCount <= 0) {
            tv.setText("Out of Stock");
            tv.setTextColor(android.graphics.Color.parseColor("#FF5252"));
            return 0;
        }

        tv.setTextColor(android.graphics.Color.parseColor("#00E5FF"));
        tv.setText("Available: " + calculatedTotalCount + " / 10");

        return (int) calculatedTotalCount;
    }

    private void initializeLevelsForSector(String sector, int price) {
        DatabaseReference sectorRef = sectorsRef.child(sector);
        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
        
        java.util.HashMap<String, Object> levels = new java.util.HashMap<>();
        levels.put("Low", createLevelMap(4));
        levels.put("Medium", createLevelMap(3));
        levels.put("High", createLevelMap(3));
        
        updates.put("levels", levels);
        updates.put("droneCount", 10L); // Total sum (4+3+3)
        updates.put("maxDrones", 10L);
        updates.put("enabled", true);
        updates.put("pricePerHour", (long) price);

        sectorRef.updateChildren(updates);
    }


    private void savePrice(String sector, int price) {
        switch (sector) {
            case "Agriculture": agriculturePrice = price; break;
            case "Delivery": deliveryPrice = price; break;
            case "Surveillance": surveillancePrice = price; break;
            case "Emergency": emergencyPrice = price; break;
            case "Wedding": weddingPrice = price; break;
            case "Construction": constructionPrice = price; break;
            case "Defense": defensePrice = price; break;
            case "Mapping": mappingPrice = price; break;
        }
    }

    private void saveMax(String sector, int max) {
        switch (sector) {
            case "Agriculture": agricultureMax = max; break;
            case "Delivery": deliveryMax = max; break;
            case "Surveillance": surveillanceMax = max; break;
            case "Emergency": emergencyMax = max; break;
            case "Wedding": weddingMax = max; break;
            case "Construction": constructionMax = max; break;
            case "Defense": defenseMax = max; break;
            case "Mapping": mappingMax = max; break;
        }
    }


    private void setupSectorClickListeners() {
        findViewById(R.id.cardAgriculture).setOnClickListener(v -> openSector("Agriculture"));
        findViewById(R.id.cardDelivery).setOnClickListener(v -> openSector("Delivery"));
        findViewById(R.id.cardSurveillance).setOnClickListener(v -> openSector("Surveillance"));
        findViewById(R.id.cardEmergency).setOnClickListener(v -> openSector("Emergency"));
        findViewById(R.id.cardWedding).setOnClickListener(v -> openSector("Wedding"));
        findViewById(R.id.cardConstruction).setOnClickListener(v -> openSector("Construction"));
        findViewById(R.id.cardDefense).setOnClickListener(v -> openSector("Defense"));
        findViewById(R.id.cardMapping).setOnClickListener(v -> openSector("Mapping"));
    }

    private void openSector(String sectorName) {
        int count = getSectorCount(sectorName);
        int max = getSectorMax(sectorName);
        int price = getSectorPrice(sectorName);
        View card = getSectorCard(sectorName);

        if (count <= 0) {
            if (card != null) shakeView(card);
            Toast.makeText(this, "Drone not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (card != null) animateClick(card);

        android.content.Intent intent = new android.content.Intent(
                SectorSelectionActivity.this,
                DroneLevelSelectionActivity.class
        );
        intent.putExtra("sector_name", sectorName);
        intent.putExtra("pricePerHour", price);
        intent.putExtra("droneCount", count);
        intent.putExtra("maxDrones", max);
        startActivity(intent);
    }

    private View getSectorCard(String sectorName) {
        switch (sectorName) {
            case "Agriculture": return findViewById(R.id.cardAgriculture);
            case "Delivery": return findViewById(R.id.cardDelivery);
            case "Surveillance": return findViewById(R.id.cardSurveillance);
            case "Emergency": return findViewById(R.id.cardEmergency);
            case "Wedding": return findViewById(R.id.cardWedding);
            case "Construction": return findViewById(R.id.cardConstruction);
            case "Defense": return findViewById(R.id.cardDefense);
            case "Mapping": return findViewById(R.id.cardMapping);
        }
        return null;
    }

    private int getSectorMax(String sector) {
        switch (sector) {
            case "Agriculture": return agricultureMax;
            case "Delivery": return deliveryMax;
            case "Surveillance": return surveillanceMax;
            case "Emergency": return emergencyMax;
            case "Wedding": return weddingMax;
            case "Construction": return constructionMax;
            case "Defense": return defenseMax;
            case "Mapping": return mappingMax;
        }
        return 10;
    }

    private int getSectorPrice(String sector) {
        switch (sector) {
            case "Agriculture": return agriculturePrice;
            case "Delivery": return deliveryPrice;
            case "Surveillance": return surveillancePrice;
            case "Emergency": return emergencyPrice;
            case "Wedding": return weddingPrice;
            case "Construction": return constructionPrice;
            case "Defense": return defensePrice;
            case "Mapping": return mappingPrice;
        }
        return 0;
    }

    private int getSectorCount(String sector) {

        switch (sector) {
            case "Agriculture": return agricultureCount;
            case "Delivery": return deliveryCount;
            case "Surveillance": return surveillanceCount;
            case "Emergency": return emergencyCount;
            case "Wedding": return weddingCount;
            case "Construction": return constructionCount;
            case "Defense": return defenseCount;
            case "Mapping": return mappingCount;
        }
        return 0;
    }

    private String getSectorName(int position) {

        switch (position) {
            case 0: return "Agriculture";
            case 1: return "Delivery";
            case 2: return "Surveillance";
            case 3: return "Emergency";
            case 4: return "Wedding";
            case 5: return "Construction";
            case 6: return "Defense";
            case 7: return "Mapping";
        }
        return "Unknown";
    }


    private void animateCards() {

        if (sectorContainer == null) return;

        int delay = 100;

        for (int i = 0; i < sectorContainer.getChildCount(); i++) {

            View card = sectorContainer.getChildAt(i);

            card.setAlpha(0f);
            card.setTranslationY(100f);

            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            delay += 100;
        }
    }

    private void animateClick(View view) {

        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 15, -15, 0);
        shake.setDuration(400);
        shake.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sectorsRef != null && sectorListener != null) {
            sectorsRef.removeEventListener(sectorListener);
        }
    }
}