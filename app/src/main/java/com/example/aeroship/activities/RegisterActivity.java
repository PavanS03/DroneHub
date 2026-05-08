package com.example.aeroship.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etPhone;
    private RadioGroup rgRole;
    private MaterialButton btnRegister;
    private TextView tvLogin, tvTitle, tvSubtitle;
    private MaterialCardView cardRegister;
    private ProgressBar passwordStrength;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        cardRegister = findViewById(R.id.cardRegister);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        passwordStrength = findViewById(R.id.passwordStrength);

        startEntryAnimation();
        startFloatingEffect(cardRegister);
        startGlowPulse(btnRegister);
        applyPremiumClickEffect(btnRegister);

        setupPasswordStrength();

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnRegister.setOnClickListener(v -> {
            animateButton();
            registerUser();
        });
    }

    private void startEntryAnimation() {
        View logo = findViewById(R.id.imgLogo);

        logo.setScaleX(0.7f);
        logo.setScaleY(0.7f);
        logo.setAlpha(0f);

        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(40f);

        tvSubtitle.setAlpha(0f);
        cardRegister.setAlpha(0f);
        cardRegister.setTranslationY(80f);

        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(900)
                .setInterpolator(new OvershootInterpolator())
                .start();

        tvTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(200)
                .start();

        tvSubtitle.animate()
                .alpha(1f)
                .setDuration(700)
                .setStartDelay(400)
                .start();

        cardRegister.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(900)
                .setStartDelay(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void setupPasswordStrength() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int strength = s.length() * 10;
                passwordStrength.setProgress(Math.min(strength, 100));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void animateButton() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(btnRegister, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(btnRegister, "scaleY", 1f, 0.95f);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(btnRegister, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(btnRegister, "scaleY", 0.95f, 1f);

        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        scaleUpX.setDuration(120);
        scaleUpY.setDuration(120);

        scaleDownX.start();
        scaleDownY.start();

        scaleUpX.setStartDelay(100);
        scaleUpY.setStartDelay(100);
        scaleUpX.start();
        scaleUpY.start();
    }

    private void applyPremiumClickEffect(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });
    }

    private void startFloatingEffect(View view) {
        view.animate()
                .translationYBy(-20f)
                .setDuration(2500)
                .withEndAction(() -> view.animate()
                        .translationYBy(20f)
                        .setDuration(2500)
                        .withEndAction(() -> startFloatingEffect(view))
                        .start())
                .start();
    }

    private void startGlowPulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.03f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.03f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.9f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(1500);
        set.start();
    }

    private void successAnimation(Runnable onComplete) {
        btnRegister.setText("Success ✓");

        btnRegister.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> btnRegister.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .withEndAction(onComplete)
                        .start())
                .start();
    }

    private void registerUser() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (name.isEmpty() || email.isEmpty()
                || password.isEmpty() || phone.isEmpty()
                || selectedRoleId == -1) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        RadioButton rbRole = findViewById(selectedRoleId);
        String roleStr = rbRole.getText().toString().toLowerCase().trim();
        final String role = roleStr;

        if (!role.equals("customer") && !role.equals("admin")) {
            Toast.makeText(this, "Invalid role selection", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Auth Error";
                        if (error.contains("already in use") && 
                           (email.equalsIgnoreCase("pruthvirajs2704@gmail.com") || 
                            email.equalsIgnoreCase("rajp51551@gmail.com"))) {
                            Toast.makeText(this, "Master Account Detected. Navigating...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }

                        Toast.makeText(this, "Auth Error: " + error, Toast.LENGTH_LONG).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("🚀 Create Account");
                        return;
                    }

                    if (auth.getCurrentUser() == null) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("🚀 Create Account");
                        return;
                    }

                    String userId = auth.getCurrentUser().getUid();

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", userId);
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("phone", phone);
                    userMap.put("role", role);
                    userMap.put("password", password); 
                    userMap.put("active", true); 
                    userMap.put("createdAt", System.currentTimeMillis());

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("USERS")
                            .child(userId)
                            .setValue(userMap)
                            .addOnCompleteListener(dbTask -> {
                                btnRegister.setEnabled(true);
                                if (dbTask.isSuccessful()) {
                                    successAnimation(() -> {
                                        auth.signOut();
                                        Toast.makeText(RegisterActivity.this, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    });
                                } else {
                                    String dbError = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown Database Error";
                                    Toast.makeText(this, "Database Error: " + dbError, Toast.LENGTH_LONG).show();
                                    if (auth.getCurrentUser() != null) {
                                        auth.getCurrentUser().delete();
                                    }
                                    btnRegister.setText("🚀 Create Account");
                                }
                            });
                });
    }
}
