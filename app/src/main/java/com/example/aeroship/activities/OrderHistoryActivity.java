package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.widget.SearchView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.BookingAdapter;
import com.example.aeroship.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private BookingAdapter bookingAdapter;
    private final List<Booking> bookingList = new ArrayList<>();

    private DatabaseReference reference;
    private ValueEventListener bookingsListener;

    private TextView tvTotalOrders, tvPendingOrders;
    private LinearLayout emptyLayout, headerLayout;
    private SearchView searchOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupRecycler();
        loadUserBookings();
        animateEntry();
    }

    private void initViews() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        emptyLayout = findViewById(R.id.emptyLayout);
        headerLayout = findViewById(R.id.headerLayout);
        searchOrders = findViewById(R.id.searchOrders);

        searchOrders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                bookingAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                bookingAdapter.filter(newText);
                return false;
            }
        });
    }

    private void setupRecycler() {

        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setHasFixedSize(true);

        bookingAdapter = new BookingAdapter(bookingList);

        bookingAdapter.setOnItemClickListener(booking -> {

            if (booking == null || booking.getBookingId() == null) {
                Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    OrderHistoryActivity.this,
                    TrackDroneActivity.class
            );

            intent.putExtra("bookingId", booking.getBookingId());
            startActivity(intent);
        });

        rvOrderHistory.setAdapter(bookingAdapter);
    }

    private void loadUserBookings() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference("ORDERS");

        bookingsListener = reference.orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        bookingList.clear();

                        int pendingCount = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Booking booking = ds.getValue(Booking.class);

                            if (booking != null) {

                                if (booking.getBookingId() == null || booking.getBookingId().isEmpty()) {
                                    booking.setBookingId(ds.getKey());
                                }

                                bookingList.add(booking);

                                String status = booking.getStatus() != null
                                        ? booking.getStatus()
                                        : "";

                                if ("Pending".equalsIgnoreCase(status)) {
                                    pendingCount++;
                                }
                            }
                        }

                        Collections.sort(bookingList, (a, b) ->
                                Long.compare(b.getTimestamp(), a.getTimestamp())
                        );

                        bookingAdapter.updateList(bookingList);

                        updateCounts(pendingCount);
                        toggleEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderHistoryActivity.this,
                                "Failed to load bookings",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCounts(int pendingCount) {

        tvTotalOrders.setText("Total: " + bookingList.size());
        tvPendingOrders.setText("Pending: " + pendingCount);
    }

    private void toggleEmptyState() {

        if (bookingList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvOrderHistory.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvOrderHistory.setVisibility(View.VISIBLE);
        }
    }

    private void animateEntry() {

        headerLayout.setAlpha(0f);
        headerLayout.setTranslationY(-120f);

        rvOrderHistory.setAlpha(0f);
        rvOrderHistory.setTranslationY(120f);

        headerLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        rvOrderHistory.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(150)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (reference != null && bookingsListener != null) {
            reference.removeEventListener(bookingsListener);
        }
    }
}