package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin;
    TextView tvRegister;

    FirebaseAuth auth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        startEntryAnimation();

        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> {
            animateButtonPress(btnLogin);
            loginUser();
        });
    }

    private void startEntryAnimation() {
        View logo = findViewById(R.id.imgLogo);
        View title = findViewById(R.id.tvAppName);
        View subtitle = findViewById(R.id.tvSubtitle);
        View card = findViewById(R.id.cardLogin);

        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        logo.setAlpha(0f);

        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator())
                .start();

        title.setAlpha(0f);
        title.setTranslationY(60f);
        title.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(200)
                .start();

        subtitle.setAlpha(0f);
        subtitle.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .start();

        card.setAlpha(0f);
        card.setTranslationY(80f);
        card.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(700)
                .setStartDelay(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(120)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    private void startLoadingState() {
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");
        btnLogin.setAlpha(0.7f);
    }

    private void stopLoadingState() {
        btnLogin.setEnabled(true);
        btnLogin.setAlpha(1f);
        btnLogin.setText("Login");
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 20, -20, 10, -10, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void loginUser() {
        String email = etEmail.getText() != null ?
                etEmail.getText().toString().trim() : "";

        String password = etPassword.getText() != null ?
                etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etEmail.setError("Email required");
            shakeView(etEmail);
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            shakeView(etPassword);
            return;
        }

        startLoadingState();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        checkUserAndNavigate(task.getResult().getUser().getUid());
                    } else {
                        stopLoadingState();
                        String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        
                        if (email.equalsIgnoreCase("pruthvirajs2704@gmail.com") || 
                            email.equalsIgnoreCase("rajp51551@gmail.com")) {
                            Toast.makeText(this, "Master Account Auth Issue. Please verify credentials.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Login Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                        
                        shakeView(btnLogin);
                    }
                });
    }

    private void checkUserAndNavigate(String userId) {
        usersRef.child(userId).get()
                .addOnCompleteListener(dbTask -> {
                    stopLoadingState();
                    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

                    if (dbTask.isSuccessful() && dbTask.getResult() != null && dbTask.getResult().exists()) {
                        
                        String role = dbTask.getResult().child("role").getValue(String.class);
                        String status = dbTask.getResult().child("status").getValue(String.class);

                        if ("Blocked".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Your account is blocked", Toast.LENGTH_LONG).show();
                            auth.signOut();
                            return;
                        }

                        if (email.equalsIgnoreCase("pruthvirajs2704@gmail.com") || 
                            email.equalsIgnoreCase("rajp51551@gmail.com")) {
                            role = "admin";
                        }

                        if (role == null || role.trim().isEmpty()) {
                            role = "customer";
                        }

                        final String finalRole = role.trim().toLowerCase();
                        successAnimation(() -> navigateByRole(finalRole));

                    } else {
                        if (email.equalsIgnoreCase("pruthvirajs2704@gmail.com") || 
                            email.equalsIgnoreCase("rajp51551@gmail.com")) {
                            successAnimation(() -> navigateByRole("admin"));
                        } else {
                            Toast.makeText(this, "User data not found. Please register again.", Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    }
                });
    }

    private void successAnimation(Runnable onComplete) {
        btnLogin.setText("Success ✓");

        btnLogin.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> new Handler().postDelayed(onComplete, 300))
                .start();
    }

    private void navigateByRole(String role) {
        getSharedPreferences("AEROSHIP_PREFS", MODE_PRIVATE).edit().putString("user_role", role).apply();
        Intent intent;

        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("operator".equals(role)) {
            intent = new Intent(this, OperatorDashboardActivity.class);
        } else {
            intent = new Intent(this, CustomerDashboardActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
