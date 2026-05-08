package com.example.aeroship.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.aeroship.R;

public class SplashActivity extends AppCompatActivity {

    private LottieAnimationView droneAnimation, particleBg;
    private LinearLayout appNameContainer;
    private TextView tvAppName, tvSubtitle, tvTagline;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        startAdvancedAnimations();
    }

    private void initViews() {
        droneAnimation = findViewById(R.id.drone_animation);
        particleBg = findViewById(R.id.particle_bg);
        appNameContainer = findViewById(R.id.app_name_container);
        tvAppName = findViewById(R.id.tv_app_name);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvTagline = findViewById(R.id.tv_tagline);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void startAdvancedAnimations() {
        particleBg.playAnimation();

        handler.postDelayed(() -> {
            droneAnimation.playAnimation();
            animateDroneEntrance();
        }, 500);

        handler.postDelayed(this::animateAppNameReveal, 1200);
        handler.postDelayed(this::animateLoadingIndicator, 2200);
        handler.postDelayed(this::navigateToMainActivity, 3500);
    }

    private void animateDroneEntrance() {
        ScaleAnimation scale = new ScaleAnimation(
                0.3f, 1.0f, 0.3f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(800);
        scale.setInterpolator(new DecelerateInterpolator());
        scale.setFillAfter(true);

        AnimationSet droneSet = new AnimationSet(true);
        droneSet.addAnimation(scale);
        droneAnimation.startAnimation(droneSet);
    }

    private void animateAppNameReveal() {
        TranslateAnimation slideUp = new TranslateAnimation(
                0, 0, 200, 0
        );
        slideUp.setDuration(600);
        slideUp.setInterpolator(new DecelerateInterpolator());

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setStartOffset(200);

        AnimationSet containerSet = new AnimationSet(true);
        containerSet.addAnimation(slideUp);
        containerSet.addAnimation(fadeIn);
        containerSet.setFillAfter(true);
        appNameContainer.startAnimation(containerSet);

        handler.postDelayed(this::pulseAppName, 1400);
    }

    private void pulseAppName() {
        ObjectAnimator glowAnimator = ObjectAnimator.ofFloat(
                tvAppName, "alpha", 1f, 0.7f, 1f
        );

        glowAnimator.setDuration(1500);
        glowAnimator.setInterpolator(new AccelerateInterpolator());
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.start();
    }

    private void animateLoadingIndicator() {
        progressBar.setVisibility(View.VISIBLE);

        ScaleAnimation pulse = new ScaleAnimation(
                0.5f, 1.1f, 0.5f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        pulse.setDuration(500);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setInterpolator(new AccelerateInterpolator());
        progressBar.startAnimation(pulse);

        tvTagline.setVisibility(View.VISIBLE);

        AlphaAnimation taglineFade = new AlphaAnimation(0f, 1f);
        taglineFade.setDuration(500);
        tvTagline.startAnimation(taglineFade);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - startTime < 8000) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }
}
