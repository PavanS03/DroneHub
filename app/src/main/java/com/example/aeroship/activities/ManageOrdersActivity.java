package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.AdminOrderAdapter;
import com.example.aeroship.models.Booking;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private AdminOrderAdapter adapter;
    private List<Booking> bookingList;
    private List<Booking> fullList = new ArrayList<>();

    private DatabaseReference ordersRef, usersRef;
    private ValueEventListener ordersListener;
    private TextView tvOrderCount, tvPendingCount;
    private LinearLayout emptyLayout;
    private ChipGroup chipGroupStatus;
    private SearchView searchOrders;

    private String statusFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        if (getIntent() != null && getIntent().hasExtra("filter")) {
            statusFilter = getIntent().getStringExtra("filter");
        }

        initViews();
        setupRecycler();
        loadOrders();
        animateEntry();
    }

    private void initViews() {

        recyclerOrders = findViewById(R.id.recyclerOrders);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        emptyLayout = findViewById(R.id.emptyLayout);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        searchOrders = findViewById(R.id.searchOrders);

        bookingList = new ArrayList<>();

        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");

        setupFilters();
        setupSearch();
    }

    private void setupSearch() {
        searchOrders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void applyFilters() {
        List<Booking> filteredList = new ArrayList<>();
        String query = searchOrders.getQuery().toString().toLowerCase().trim();

        for (Booking booking : fullList) {
            String status = (booking.getStatus() != null) ? booking.getStatus() : "Pending";
            
            boolean statusMatch = statusFilter.equalsIgnoreCase("all") || status.equalsIgnoreCase(statusFilter);
            
            boolean queryMatch = query.isEmpty() || 
                                (booking.getUserName() != null && booking.getUserName().toLowerCase().contains(query)) ||
                                (booking.getSector() != null && booking.getSector().toLowerCase().contains(query)) ||
                                (booking.getBookingId() != null && booking.getBookingId().toLowerCase().contains(query));
            
            if (statusMatch && queryMatch) {
                filteredList.add(booking);
            }
        }

        bookingList.clear();
        bookingList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        tvOrderCount.setText("Total: " + bookingList.size());
        
        if (bookingList.isEmpty()) showEmpty();
        else showList();
    }

    private void setupFilters() {
        if (statusFilter.equalsIgnoreCase("pending")) chipGroupStatus.check(R.id.chipPending);
        else if (statusFilter.equalsIgnoreCase("approved")) chipGroupStatus.check(R.id.chipApproved);
        else if (statusFilter.equalsIgnoreCase("ongoing")) chipGroupStatus.check(R.id.chipOngoing);
        else if (statusFilter.equalsIgnoreCase("completed")) chipGroupStatus.check(R.id.chipCompleted);
        else if (statusFilter.equalsIgnoreCase("rejected")) chipGroupStatus.check(R.id.chipRejected);
        else chipGroupStatus.check(R.id.chipAll);

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chipPending) statusFilter = "Pending";
            else if (checkedId == R.id.chipApproved) statusFilter = "Approved";
            else if (checkedId == R.id.chipOngoing) statusFilter = "Ongoing";
            else if (checkedId == R.id.chipCompleted) statusFilter = "Completed";
            else if (checkedId == R.id.chipRejected) statusFilter = "Rejected";
            else statusFilter = "all";

            applyFilters();
        });
    }

    private void setupRecycler() {

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setHasFixedSize(true);

        adapter = new AdminOrderAdapter(bookingList);
        recyclerOrders.setAdapter(adapter);
    }

    private void loadOrders() {

        ordersListener = ordersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                fullList.clear();
                int pendingCount = 0;

                if (!snapshot.exists()) {
                    applyFilters();
                    tvPendingCount.setText("Pending: 0");
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Booking booking = ds.getValue(Booking.class);
                    if (booking == null) continue;

                    booking.setBookingId(ds.getKey());

                    String status = (booking.getStatus() != null) ? booking.getStatus() : "Pending";

                    if (status.equalsIgnoreCase("Pending")) {
                        pendingCount++;
                    }

                    fetchUserName(booking);

                    fullList.add(booking);
                }

                Collections.sort(fullList, (a, b) ->
                        Long.compare(b.getTimestamp(), a.getTimestamp())
                );

                tvPendingCount.setText("Pending: " + pendingCount);
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageOrdersActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    private void fetchUserName(Booking booking) {

        if (booking.getUserId() == null) {
            booking.setUserName("Unknown");
            return;
        }

        usersRef.child(booking.getUserId())
                .child("name") 
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            booking.setUserName(snapshot.getValue(String.class));
                        } else {
                            booking.setUserName("Unknown");
                        }

                        applyFilters(); 
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        booking.setUserName("Unknown");
                    }
                });
    }

    private void showEmpty() {
        emptyLayout.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
    }

    private void showList() {
        emptyLayout.setVisibility(View.GONE);
        recyclerOrders.setVisibility(View.VISIBLE);
    }

    private void animateEntry() {

        recyclerOrders.setAlpha(0f);
        recyclerOrders.setTranslationY(120f);

        recyclerOrders.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }
}