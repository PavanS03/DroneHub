package com.example.aeroship.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.example.aeroship.R;
import com.example.aeroship.models.SupportMessage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView tvUserName;
    private MaterialCardView notificationBell;
    private MaterialCardView notificationBadge;
    private Chip premiumBadge;

    private LottieAnimationView animBgDrone;
    private LottieAnimationView animParticles;

    private ViewPager2 statsCarousel;
    private LinearLayout statsIndicators;

    private MaterialCardView btnBookDrone;
    private MaterialCardView btnTrackFlight;
    private MaterialCardView btnWatchHistory;
    private MaterialCardView btnViewReports;
    private FloatingActionButton fabQuickRent;

    private BottomNavigationView bottomNavigation;

    private int totalOrders = 127;
    private int activeOrders = 3;
    private int completedOrders = 124;
    private String userGreeting = "Good Morning!";
    private int notifications = 3;

    private StatsPagerAdapter statsAdapter;
    private List<StatsItem> liveStats = new ArrayList<>();
    private com.google.firebase.database.DatabaseReference sectorsRef;
    private com.google.firebase.database.ValueEventListener statsListener;

    public static class StatsItem {
        public String title;
        public String value;
        public int iconResId;
        public String colorHex;

        public StatsItem(String title, String value, int iconResId, String colorHex) {
            this.title = title;
            this.value = value;
            this.iconResId = iconResId;
            this.colorHex = colorHex;
        }
    }

    public class StatsPagerAdapter extends RecyclerView.Adapter<StatsPagerAdapter.StatsViewHolder> {
        private List<StatsItem> statsList;

        public StatsPagerAdapter(List<StatsItem> statsList) {
            this.statsList = statsList;
        }

        public void updateData(List<StatsItem> newList) {
            this.statsList = new ArrayList<>(newList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stats_card, parent, false);
            return new StatsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
            StatsItem item = statsList.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvValue.setText(item.value);
            if (holder.ivStatIcon != null) {
                holder.ivStatIcon.setImageResource(item.iconResId);
                try {
                    holder.ivStatIcon.setColorFilter(Color.parseColor(item.colorHex));
                } catch (Exception e) {
                    holder.ivStatIcon.setColorFilter(Color.CYAN);
                }
            }
        }

        @Override
        public int getItemCount() {
            return statsList != null ? statsList.size() : 0;
        }

        public class StatsViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTitle, tvValue;
            public android.widget.ImageView ivStatIcon;

            public StatsViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvValue = itemView.findViewById(R.id.tvValue);
                ivStatIcon = itemView.findViewById(R.id.ivStatIcon);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        sectorsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("SECTORS");

        initViews();
        setupRecyclerViews();
        setupAnimations();
        setupClickListeners();
        startHeroAnimation();
        updateUI();
        listenToLiveStats();
    }

    private void listenToLiveStats() {
        statsListener = sectorsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                long totalAvailable = 0;
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    Long count = ds.child("droneCount").getValue(Long.class);
                    if (count != null) totalAvailable += count;
                }
                
                liveStats.clear();
                liveStats.add(new StatsItem("Total Drones", String.valueOf(totalAvailable), R.drawable.ic_drone_fab, "#00D4FF"));
                liveStats.add(new StatsItem("Active Rentals", "0", R.drawable.ic_live_tracking, "#00E676"));
                liveStats.add(new StatsItem("Success Rate", "100%", R.drawable.ic_check_circle, "#FFD700"));
                
                if (statsAdapter != null) {
                    statsAdapter.updateData(liveStats);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void initViews() {
        // Header
        tvUserName = findViewById(R.id.tvUserName);
        notificationBell = findViewById(R.id.notificationBell);
        notificationBadge = findViewById(R.id.notificationBadge);
        premiumBadge = findViewById(R.id.premiumBadge);

        // Animations
        animBgDrone = findViewById(R.id.animation_bg_drone);
        animParticles = findViewById(R.id.animation_particles);

        // Carousel
        statsCarousel = findViewById(R.id.statsCarousel);
        statsIndicators = findViewById(R.id.statsIndicators);

        // Quick Actions
        btnBookDrone = findViewById(R.id.btnBookDrone);
        btnTrackFlight = findViewById(R.id.btnTrackFlight);
        btnWatchHistory = findViewById(R.id.btnWatchHistory);
        btnViewReports = findViewById(R.id.btnViewReports);
        fabQuickRent = findViewById(R.id.fabQuickRent);

        // Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerViews() {
        // Stats Carousel
        liveStats = getSampleStats();
        statsAdapter = new StatsPagerAdapter(liveStats);
        statsCarousel.setAdapter(statsAdapter);
        statsCarousel.setOffscreenPageLimit(3);
        setupStatsCarousel();
    }

    private void setupStatsCarousel() {
        statsCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStatsIndicators(position);
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (statsAdapter != null && statsCarousel != null) {
                    int current = statsCarousel.getCurrentItem();
                    int next = (current + 1) % statsAdapter.getItemCount();
                    statsCarousel.setCurrentItem(next, true);
                    handler.postDelayed(this, 4000);
                }
            }
        };
        handler.postDelayed(scrollRunnable, 4000);
    }

    private List<StatsItem> getSampleStats() {
        List<StatsItem> stats = new ArrayList<>();
        stats.add(new StatsItem("Total Drones", "80", R.drawable.ic_drone_fab, "#00D4FF"));
        stats.add(new StatsItem("Active Rentals", "0", R.drawable.ic_live_tracking, "#00E676"));
        stats.add(new StatsItem("Success Rate", "100%", R.drawable.ic_check_circle, "#FFD700"));
        return stats;
    }

    private void setupAnimations() {
        if (animBgDrone != null) animBgDrone.playAnimation();
        if (animParticles != null) animParticles.playAnimation();
        new Handler(Looper.getMainLooper()).postDelayed(this::animateEntrance, 300);
    }

    private void startHeroAnimation() {
        if (animParticles != null) {
            animParticles.setSpeed(1.5f);
            animParticles.playAnimation();
        }

        if (animBgDrone != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(animBgDrone, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(animBgDrone, "scaleY", 0.5f, 1f);
            AnimatorSet scaleSet = new AnimatorSet();
            scaleSet.playTogether(scaleX, scaleY);
            scaleSet.setDuration(1200);
            scaleSet.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleSet.start();
        }
    }

    private void animateEntrance() {
        long staggerDelay = 100;

        View headerGlass = findViewById(R.id.headerGlass);
        animateView(headerGlass, 0);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateView(tvUserName, staggerDelay);
            animateView(notificationBell, staggerDelay * 2);
        }, 200);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateView(btnBookDrone, 0);
            new Handler(Looper.getMainLooper()).postDelayed(() -> animateView(btnTrackFlight, staggerDelay), staggerDelay);
            new Handler(Looper.getMainLooper()).postDelayed(() -> animateView(btnWatchHistory, staggerDelay * 2), staggerDelay * 2);
            new Handler(Looper.getMainLooper()).postDelayed(() -> animateView(btnViewReports, staggerDelay * 3), staggerDelay * 3);
        }, 600);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View appDetails = findViewById(R.id.appDetailsCard);
            animateView(appDetails, 0);
        }, 1200);
    }

    private void animateView(View view, long delay) {
        if (view == null) return;

        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setTranslationY(50f);

        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void updateUI() {
        if (tvUserName != null) {
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            String greeting = (hour < 12) ? "Good Morning"
                    : (hour < 17) ? "Good Afternoon"
                    : (hour < 21) ? "Good Evening" : "Good Night";
            
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("USERS")
                        .child(user.getUid())
                        .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                String name = snapshot.child("name").getValue(String.class);
                                if (name == null || name.isEmpty()) name = "User";
                                tvUserName.setText(greeting + ", " + name + "!");
                            }
                            @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                        });
            } else {
                tvUserName.setText(greeting + "!");
            }
        }
        updateNotificationBadge();
        showPremiumStatus();
    }

    private void setupClickListeners() {

        setupQuickAction(btnBookDrone, SectorSelectionActivity.class);
        setupQuickAction(btnTrackFlight, TrackDroneActivity.class);
        setupQuickAction(btnWatchHistory, OrderHistoryActivity.class);
        setupQuickAction(btnViewReports, UserSupportActivity.class);

        if (fabQuickRent != null) {
            fabQuickRent.setOnClickListener(v -> {
                scaleFABAnimation(fabQuickRent);
                startActivity(new Intent(this, SectorSelectionActivity.class));
            });
        }

        if (notificationBell != null) {
            notificationBell.setOnClickListener(v -> {
                pulseNotification();
                startActivity(new Intent(this, NotificationActivity.class));
            });
        }

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {

                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                }

                else if (itemId == R.id.nav_bookings) {
                    startActivity(new Intent(this, OrderHistoryActivity.class));
                    return true;
                }

                else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                }

                return false;
            });
        }
    }
    private void setupQuickAction(MaterialCardView button, Class<?> activityClass) {
        if (button != null) {
            button.setOnClickListener(v -> playButtonAnimation(v, () -> {
                Intent intent = new Intent(CustomerDashboardActivity.this, activityClass);
                if (activityClass == TrackDroneActivity.class) {
                    String bookingId = "BOOK123"; 
                    intent.putExtra("bookingId", bookingId);
                }
                startActivity(intent);
                playSuccessAnimation(v);
            }));
        }
    }

    private void playButtonAnimation(View view, Runnable onEnd) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd.run();
            }
        });
        animatorSet.start();
    }

    private void playSuccessAnimation(View view) {
        if (view instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) view;
            int startColor = Color.parseColor("#00D4FF");
            int endColor = Color.WHITE;

            ObjectAnimator glow = ObjectAnimator.ofArgb(cardView, "cardBackgroundColor",
                    startColor, endColor, startColor);
            glow.setDuration(600);
            glow.setRepeatCount(1);
            glow.setRepeatMode(ValueAnimator.REVERSE);
            glow.start();
        }
    }

    private void scaleFABAnimation(FloatingActionButton fab) {
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(fab, "rotation", 0f, 360f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnimator, rotateAnimator);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    private void pulseNotification() {
        if (notificationBadge != null) {
            ObjectAnimator pulseX = ObjectAnimator.ofFloat(notificationBadge, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator pulseY = ObjectAnimator.ofFloat(notificationBadge, "scaleY", 1f, 1.3f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(pulseX, pulseY);
            animatorSet.setDuration(300);
            animatorSet.start();
        }
    }

    private void animateCounter(TextView textView, int targetValue) {
        if (textView == null) return;
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            textView.setScaleX(1f + (float)(Math.sin(value * 0.1) * 0.02));
        });
        animator.start();
    }

    private void updateNotificationBadge() {
        if (notificationBadge != null) {
            TextView badgeText = notificationBadge.findViewById(R.id.tvBadgeCount);
            if (badgeText != null) {
                badgeText.setText(String.valueOf(notifications));
                badgeText.setAlpha(1f);
            }
            notificationBadge.setVisibility(View.VISIBLE);
        }
    }

    private void showPremiumStatus() {
        if (premiumBadge != null) {
            premiumBadge.setVisibility(View.VISIBLE);
            premiumBadge.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void updateStatsIndicators(int selectedPosition) {
        if (statsIndicators != null && statsIndicators.getChildCount() > 0) {
            for (int i = 0; i < Math.min(statsIndicators.getChildCount(), 3); i++) {
                View indicator = statsIndicators.getChildAt(i);
                if (indicator != null) {
                    float alpha = i == selectedPosition ? 1f : 0.3f;
                    float scale = i == selectedPosition ? 1.2f : 1f;
                    indicator.animate()
                            .alpha(alpha)
                            .scaleX(scale)
                            .scaleY(scale)
                            .setDuration(300)
                            .start();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animBgDrone != null) animBgDrone.pauseAnimation();
        if (animParticles != null) animParticles.pauseAnimation();
        if (sectorsRef != null && statsListener != null) {
            sectorsRef.removeEventListener(statsListener);
        }
    }
}