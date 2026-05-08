package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.BookingAdapter;
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

public class UserOrdersActivity extends AppCompatActivity {

    private String userId, userName;
    private TextView tvUserName, tvOrderCount;
    private ImageView btnBack;
    private RecyclerView recyclerUserOrders;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private List<Booking> bookingList = new ArrayList<>();
    private List<Booking> fullList = new ArrayList<>();
    private BookingAdapter adapter;
    private DatabaseReference reference;
    private SearchView searchOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        if (userId == null) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvUserName = findViewById(R.id.tvUserName);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        btnBack = findViewById(R.id.btnBack);
        recyclerUserOrders = findViewById(R.id.recyclerUserOrders);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        searchOrders = findViewById(R.id.searchOrders);

        tvUserName.setText(userName != null ? userName + "'s Orders" : "User Orders");

        recyclerUserOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList);
        recyclerUserOrders.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference("BOOKINGS");

        setupSearch();
        loadUserOrders();

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        searchOrders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        List<Booking> filteredList = new ArrayList<>();
        String searchText = query.toLowerCase().trim();

        for (Booking item : fullList) {
            if (item.getSector().toLowerCase().contains(searchText) ||
                    item.getStatus().toLowerCase().contains(searchText) ||
                    item.getBookingId().toLowerCase().contains(searchText)) {
                filteredList.add(item);
            }
        }

        bookingList.clear();
        bookingList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (bookingList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void loadUserOrders() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Query query = reference.orderByChild("userId").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking booking = ds.getValue(Booking.class);
                    if (booking != null) {
                        fullList.add(booking);
                    }
                }

                Collections.sort(fullList, (b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));

                filter(searchOrders.getQuery().toString());
                
                progressBar.setVisibility(View.GONE);

                tvOrderCount.setText("Total Orders: " + fullList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserOrdersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
