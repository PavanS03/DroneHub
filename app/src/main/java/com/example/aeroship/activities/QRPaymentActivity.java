package com.example.aeroship.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;

public class QRPaymentActivity extends AppCompatActivity {

    private TextView tvAmount;
    private ImageView imgQR;
    private MaterialButton btnPaymentDone;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrpayment);

        tvAmount = findViewById(R.id.tvAmount);
        imgQR = findViewById(R.id.imgQR);
        btnPaymentDone = findViewById(R.id.btnPaymentDone);

        int amount = getIntent().getIntExtra("amount", 0);
        bookingId = getIntent().getStringExtra("bookingId");

        tvAmount.setText("₹ " + amount);

        btnPaymentDone.setOnClickListener(v -> {
            Intent intent = new Intent(QRPaymentActivity.this, PaymentProofActivity.class);
            intent.putExtra("orderId", bookingId);
            startActivity(intent);
            finish();
        });
    }
}
