package com.example.aeroship.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.example.aeroship.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvAdminName, tvPendingCount, tvApprovedCount,
            tvRejectedCount, tvTotalRevenue, tvCompletedCount;
    private MaterialCardView cardPending, cardApproved, cardRejected, cardCompleted, cardRevenue;
    private LineChart lineChart;
    private LottieAnimationView lottieParticles, lottieRevenueDrone;
    private MotionLayout motionLayout;
    private FloatingActionButton fabQuickStats, btnNotifications, btnLogout;

    private MaterialButton btnManageOrders, btnManageOperators, btnUserOrderDetails,
            btnOperatorMissionDetails, btnManageSectors, btnSupport, btnManagePayments;

    private DatabaseReference ordersRef, usersRef;
    private ValueEventListener statsListener;
    private FirebaseAuth auth;

    SharedPreferences preferences;
    private int lastOrderCount = 0;

    private boolean isAnimating = false;

    private Handler animationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        initFirebase();
        initViews();
        setupToolbar();
        setupAnimations();
        setupNavigation();
        setupChart();
        loadStats();
        setupNotificationChannel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            showNotificationPulse();
            return true;
        } else if (id == R.id.action_search) {
            Toast.makeText(this, "🔍 Search functionality", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "⚙️ Settings", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_live_map) {
            Toast.makeText(this, "🗺️ Live Map", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_emergency) {
            Toast.makeText(this, "🚨 Emergency Mode", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (auth != null) {
                        auth.signOut();
                    }
                    SharedPreferences preferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
                    preferences.edit().clear().apply();
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void initFirebase() {
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvRejectedCount = findViewById(R.id.tvRejectedCount);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);

        cardPending = findViewById(R.id.cardPending);
        cardApproved = findViewById(R.id.cardApproved);
        cardRejected = findViewById(R.id.cardRejected);
        cardCompleted = findViewById(R.id.cardCompleted);
        cardRevenue = findViewById(R.id.cardRevenue);

        lineChart = findViewById(R.id.lineChart);
        lottieParticles = findViewById(R.id.lottieParticles);
        lottieRevenueDrone = findViewById(R.id.lottieRevenueDrone);
        motionLayout = findViewById(R.id.motionLayout);

        fabQuickStats = findViewById(R.id.fabQuickStats);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnLogout = findViewById(R.id.btnLogout);

        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageOperators = findViewById(R.id.btnManageOperators);
        btnUserOrderDetails = findViewById(R.id.btnUserOrderDetails);
        btnOperatorMissionDetails = findViewById(R.id.btnOperatorMissionDetails);
        btnManageSectors = findViewById(R.id.btnManageSectors);
        btnSupport = findViewById(R.id.btnSupport);
        btnManagePayments = findViewById(R.id.btnManagePayments);
    }

    private void setupAnimations() {
        if (motionLayout != null) {
            motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {}
                @Override
                public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {}
                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                    animateStatsCards();
                }
                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {}
            });
        }

        startFabPulse();

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationPulse());
        }

        setupCardAnimations();
    }

    private void animateStatsCards() {
        if (isAnimating) return;
        isAnimating = true;

        ObjectAnimator pendingAnim = ObjectAnimator.ofFloat(cardPending, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator pendingY = ObjectAnimator.ofFloat(cardPending, "translationY", 50f, 0f);
        ObjectAnimator approvedAnim = ObjectAnimator.ofFloat(cardApproved, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator revenueAnim = ObjectAnimator.ofFloat(cardRevenue, "scaleX", 0f, 1.1f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.play(pendingAnim).with(pendingY).before(approvedAnim);
        set.setDuration(600);
        set.setInterpolator(new OvershootInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        set.start();
    }

    private void startFabPulse() {
        if (fabQuickStats == null) return;

        ValueAnimator pulse = ValueAnimator.ofFloat(1f, 0.95f, 1f);
        pulse.setDuration(1200);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.RESTART);
        pulse.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            fabQuickStats.setScaleX(scale);
            fabQuickStats.setScaleY(scale);
        });
        pulse.start();

        fabQuickStats.setOnClickListener(v -> showQuickStatsDialog());
    }

    private void setupCardAnimations() {
        View.OnClickListener cardClick = v -> {
            MaterialCardView card = (MaterialCardView) v;
            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.05f);
            ObjectAnimator scaleDown = ObjectAnimator.ofFloat(card, "scaleX", 1.05f, 1f);
            AnimatorSet clickAnim = new AnimatorSet();
            clickAnim.playSequentially(scaleUp, scaleDown);
            clickAnim.setDuration(150);
            clickAnim.start();
        };

        if (cardPending != null) cardPending.setOnClickListener(cardClick);
        if (cardApproved != null) cardApproved.setOnClickListener(cardClick);
        if (cardRejected != null) cardRejected.setOnClickListener(cardClick);
        if (cardCompleted != null) cardCompleted.setOnClickListener(cardClick);
    }

    private void showNotificationPulse() {
        if (btnNotifications == null) return;
        ObjectAnimator pulse = ObjectAnimator.ofFloat(btnNotifications, "scaleX", 1f, 1.3f, 1f);
        pulse.setDuration(300);
        pulse.start();
        Toast.makeText(this, "🔔 3 New Notifications", Toast.LENGTH_SHORT).show();
    }

    private void loadStats() {
        statsListener = ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int pending = 0, approved = 0, rejected = 0, completed = 0;
                    double totalOrderValue = 0, totalAdvanceRevenue = 0;
                    int currentOrderCount = (int) snapshot.getChildrenCount();

                    if (lastOrderCount != 0 && currentOrderCount > lastOrderCount) {
                        showNotification(" New Drone Booking!", "A new rental request arrived!");
                        playNewOrderAnimation();
                    }
                    lastOrderCount = currentOrderCount;

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String status = ds.child("status").getValue(String.class);
                        Integer amount = ds.child("totalAmount").getValue(Integer.class);
                        Boolean advancePaid = ds.child("advancePaid").getValue(Boolean.class);

                        if (status == null) status = "Pending";

                        switch (status) {
                            case "Approved":
                                approved++;
                                break;
                            case "Rejected":
                                rejected++;
                                break;
                            case "Completed":
                                completed++;
                                break;
                            default:
                                pending++;
                                break;
                        }

                        if (amount != null) {
                            totalOrderValue += amount;
                            if (Boolean.TRUE.equals(advancePaid)) {
                                totalAdvanceRevenue += (amount * 0.2);
                            }
                        }
                    }

                    updateStatsWithAnimation(pending, approved, rejected, completed,
                            totalOrderValue, totalAdvanceRevenue);

                    updateChart(pending, approved, rejected, completed);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AdminDashboardActivity.this, "Error loading stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatsWithAnimation(int pending, int approved, int rejected,
                                          int completed, double totalOrderValue, double advanceRevenue) {

        animateCounter(tvPendingCount, getIntFromText(tvPendingCount), pending);
        animateCounter(tvApprovedCount, getIntFromText(tvApprovedCount), approved);
        animateCounter(tvRejectedCount, getIntFromText(tvRejectedCount), rejected);
        animateCounter(tvCompletedCount, getIntFromText(tvCompletedCount), completed);

        animateRevenueCounter(tvTotalRevenue, getDoubleFromText(tvTotalRevenue), (float) advanceRevenue);
    }

    private int getIntFromText(TextView textView) {
        try {
            return Integer.parseInt(textView.getText().toString().replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDoubleFromText(TextView textView) {
        try {
            return Double.parseDouble(textView.getText().toString().replace("₹", "").replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void animateCounter(TextView counterView, int startValue, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            counterView.setText(String.format(Locale.getDefault(), "%,d", value));
        });
        animator.start();
    }

    private void animateRevenueCounter(TextView revenueView, double startValue, float endValue) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) startValue, endValue);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            revenueView.setText(String.format(Locale.getDefault(), "₹%,.0f", (double) value));
        });
        animator.start();
    }

    private void setupChart() {
        if (lineChart == null) return;

        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawBorders(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }

    private void updateChart(int pending, int approved, int rejected, int completed) {
        if (lineChart == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, pending));
        entries.add(new Entry(1, approved));
        entries.add(new Entry(2, rejected));
        entries.add(new Entry(3, completed));

        LineDataSet dataSet = new LineDataSet(entries, "Flight Orders");
        dataSet.setColor(Color.parseColor("#009688"));
        dataSet.setCircleColor(Color.parseColor("#80D8FF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(6f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(0f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.animateXY(1000, 1000);
        lineChart.invalidate();
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "orders", "Drone Orders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("New booking notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "orders")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(new Random().nextInt(1000), builder.build());
        }
    }

    private void playNewOrderAnimation() {
        if (lottieRevenueDrone != null) {
            lottieRevenueDrone.setSpeed(2f);
        }
        if (cardRevenue != null) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(cardRevenue,
                    "translationX", 0f, 10f, -10f, 10f, -10f, 0f);
            shake.setDuration(500);
            shake.start();
        }
    }

    private void setupNavigation() {
        setupGreeting();

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                ObjectAnimator ripple = ObjectAnimator.ofFloat(btnLogout, "scaleX", 1f, 0.9f, 1f);
                ripple.setDuration(200);
                ripple.start();
                logoutUser();
            });
        }

        View.OnClickListener navClick = v -> {
            MaterialButton btn = (MaterialButton) v;
            ObjectAnimator ripple = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 0.95f, 1f);
            ripple.setDuration(200);
            ripple.start();

            navigateToActivity(btn.getId());
        };

        if (btnManageOrders != null) btnManageOrders.setOnClickListener(navClick);
        if (btnManageOperators != null) btnManageOperators.setOnClickListener(navClick);
        if (btnUserOrderDetails != null) btnUserOrderDetails.setOnClickListener(navClick);
        if (btnOperatorMissionDetails != null) btnOperatorMissionDetails.setOnClickListener(navClick);
        if (btnManageSectors != null) btnManageSectors.setOnClickListener(navClick);
        if (btnSupport != null) btnSupport.setOnClickListener(navClick);
        if (btnManagePayments != null) btnManagePayments.setOnClickListener(navClick);
    }

    private void navigateToActivity(int buttonId) {
        Intent intent = null;

        if (buttonId == R.id.btnManageOrders) {
            intent = new Intent(this, ManageOrdersActivity.class);
        } else if (buttonId == R.id.btnManageOperators) {
            intent = new Intent(this, ManageOperatorsActivity.class);
        } else if (buttonId == R.id.btnUserOrderDetails) {
            intent = new Intent(this, UserManagementActivity.class);
        } else if (buttonId == R.id.btnOperatorMissionDetails) {
            intent = new Intent(this, ManageOperatorsActivity.class);
        } else if (buttonId == R.id.btnManageSectors) {
            intent = new Intent(this, ManageSectorsActivity.class);
        } else if (buttonId == R.id.btnSupport) {
            intent = new Intent(this, AdminSupportActivity.class);
        } else if (buttonId == R.id.btnManagePayments) {
            intent = new Intent(this, AdminPaymentApprovalActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGreeting() {
        if (tvGreeting == null) return;
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good Morning"
                : (hour < 17) ? "Good Afternoon"
                : (hour < 21) ? "Good Evening" : "Good Night";
        
        loadAdminName(greeting);
    }

    private void loadAdminName(String greeting) {
        if (auth.getCurrentUser() == null) {
            tvGreeting.setText(greeting + "!");
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (tvGreeting != null) {
                    tvGreeting.setText(greeting + ", " + (name != null ? name : "Admin") + "!");
                }
                if (tvAdminName != null) {
                    tvAdminName.setText(name != null ? name : "Drone Control Center");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showQuickStatsDialog() {
        String stats = String.format(Locale.getDefault(),
                "📊 Quick Stats\nPending: %d | Approved: %d\nRevenue: ₹%.0f\nCompleted: %d",
                getIntFromText(tvPendingCount),
                getIntFromText(tvApprovedCount),
                getDoubleFromText(tvTotalRevenue),
                getIntFromText(tvCompletedCount));
        Toast.makeText(this, stats, Toast.LENGTH_LONG).show();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "🔐 Session expired. Please login again.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && statsListener != null) {
            ordersRef.removeEventListener(statsListener);
        }
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
}