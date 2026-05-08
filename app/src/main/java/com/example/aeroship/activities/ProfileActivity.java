package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar, tvName, tvEmail, tvPhone, tvRole, tvJoinDate;
    private MaterialButton btnLogout, btnEdit;
    private MaterialCardView profileCard;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        initViews();
        loadUserData();
        applyEntryAnimation();

        btnLogout.setOnClickListener(v -> logoutUser());

        btnEdit.setOnClickListener(v -> {
            animateButton(v);
            Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {

        tvAvatar = findViewById(R.id.tvAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);
        tvJoinDate = findViewById(R.id.tvJoinDate);

        btnLogout = findViewById(R.id.btnLogout);
        btnEdit = findViewById(R.id.btnEdit);
        profileCard = findViewById(R.id.profileCard);
    }

    private void loadUserData() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        userRef = FirebaseDatabase.getInstance()
                .getReference("USERS")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);
                Long createdAt = snapshot.child("createdAt").getValue(Long.class);

                tvName.setText(name != null ? name : "N/A");
                tvEmail.setText(user.getEmail());
                tvPhone.setText(phone != null ? phone : "N/A");
                tvRole.setText(role != null ? role : "User");

                if (name != null && !name.isEmpty()) {
                    tvAvatar.setText(name.substring(0, 1).toUpperCase());
                }

                if (createdAt != null) {
                    String formattedDate = new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                    ).format(new Date(createdAt));

                    tvJoinDate.setText("Joined: " + formattedDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logoutUser() {

        auth.signOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void applyEntryAnimation() {

        profileCard.setTranslationY(200f);
        profileCard.setAlpha(0f);

        profileCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start())
                .start();
    }
}
