package com.example.aeroship.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.UserAdapter;
import com.example.aeroship.models.User;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private TextView tvTotalUsers, tvEmpty;
    private ImageView btnBack;
    private EditText etSearch; // 🔍 NEW

    private List<User> userList = new ArrayList<>();
    private List<User> fullList = new ArrayList<>();

    private UserAdapter adapter;

    private DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        btnBack = findViewById(R.id.btnBack);

        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setHasFixedSize(true);

        adapter = new UserAdapter(userList);
        recyclerUsers.setAdapter(adapter);

        adapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(this, UserOrdersActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userName", user.getName());
            startActivity(intent);
        });

        reference = FirebaseDatabase.getInstance().getReference("USERS");

        loadUsers();
        animateCounter();
        setupSearch(); // 🔍

        btnBack.setOnClickListener(v -> finish());
    }


    private void loadUsers() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userList.clear();
                fullList.clear();

                if (!snapshot.exists()) {
                    tvTotalUsers.setText("0");
                    showEmpty(true);
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {

                    User user = ds.getValue(User.class);

                    if (user != null) {

                        user.setUserId(ds.getKey());

                        String role = user.getRole() != null
                                ? user.getRole().toLowerCase().trim()
                                : "";

                        if (role.equals("user") || role.equals("customer")) {

                            userList.add(user);
                            fullList.add(user);
                        }
                    }
                }

                tvTotalUsers.setText(String.valueOf(userList.size()));
                adapter.notifyDataSetChanged();

                // 🔥 EMPTY STATE
                showEmpty(userList.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(UserManagementActivity.this,
                        "Failed to load users",
                        Toast.LENGTH_SHORT).show();

                showEmpty(true);
            }
        });
    }


    private void setupSearch() {

        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {

                String query = s.toString().toLowerCase();

                userList.clear();

                for (User user : fullList) {

                    if ((user.getName() != null &&
                            user.getName().toLowerCase().contains(query)) ||

                            (user.getEmail() != null &&
                                    user.getEmail().toLowerCase().contains(query)) ||

                            (user.getPhone() != null &&
                                    user.getPhone().contains(query))) {

                        userList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
                showEmpty(userList.isEmpty());
            }
        });
    }


    private void showEmpty(boolean isEmpty) {

        if (tvEmpty == null) return;

        if (isEmpty) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerUsers.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerUsers.setVisibility(View.VISIBLE);
        }
    }


    private void animateCounter() {

        tvTotalUsers.setScaleX(0f);
        tvTotalUsers.setScaleY(0f);

        tvTotalUsers.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}