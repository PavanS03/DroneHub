package com.example.aeroship.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etReview;
    private MaterialButton btnSubmit;

    private DatabaseReference ordersRef;

    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmit = findViewById(R.id.btnSubmit);

        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            Toast.makeText(this, "Invalid Booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        float rating = ratingBar.getRating();
        String review = etReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please give rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(review)) {
            review = "No review given";
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("review", review);

        ordersRef.child(bookingId)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            RatingActivity.this,
                            "Thanks for your feedback ⭐",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit");

                    Toast.makeText(
                            RatingActivity.this,
                            "Failed to submit rating",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
