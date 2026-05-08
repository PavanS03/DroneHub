package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.NotificationAdapter;
import com.example.aeroship.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> list;

    private DatabaseReference notificationRef, ordersRef;

    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerNotifications);
        tvEmpty = findViewById(R.id.tvEmpty); 

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new NotificationAdapter(list);
        recyclerView.setAdapter(adapter);

        loadNotifications();
        animateCard();
    }

    private void loadNotifications() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showEmpty("User not logged in");
            return;
        }

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        notificationRef = FirebaseDatabase
                .getInstance()
                .getReference("NOTIFICATIONS")
                .child(userId);

        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    NotificationModel model =
                            ds.getValue(NotificationModel.class);

                    if (model != null) {
                        list.add(model);
                    }
                }

                if (list.isEmpty()) {
                    loadFromOrders(userId);
                } else {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmpty("Failed to load notifications");
            }
        });
    }

    private void loadFromOrders(String userId) {

        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference("ORDERS");

        ordersRef.orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        list.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String message = ds.child("notification")
                                    .getValue(String.class);

                            if (message != null && !message.isEmpty()) {

                                NotificationModel model =
                                        new NotificationModel();

                                model.setMessage(message);
                                model.setTimestamp(
                                        ds.child("timestamp")
                                                .getValue(Long.class) != null
                                                ? ds.child("timestamp")
                                                .getValue(Long.class)
                                                : System.currentTimeMillis()
                                );

                                list.add(model);
                            }
                        }

                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showEmpty("Failed to load orders");
                    }
                });
    }

    private void updateUI() {

        adapter.notifyDataSetChanged();

        if (list.isEmpty()) {
            showEmpty("No Notifications");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(String message) {

        recyclerView.setVisibility(View.GONE);

        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(message);
        }
    }

    private void animateCard() {

        View card = findViewById(R.id.notificationCard);

        if (card != null) {
            ObjectAnimator.ofFloat(card, "translationY", 200f, 0f)
                    .setDuration(600)
                    .start();
        }
    }
}