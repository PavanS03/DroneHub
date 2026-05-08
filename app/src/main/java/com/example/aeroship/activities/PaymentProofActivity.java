package com.example.aeroship.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.aeroship.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class PaymentProofActivity extends AppCompatActivity {

    private EditText etTxnId;
    private ImageView ivPreview;
    private MaterialButton btnUpload, btnSubmit;
    private Uri imageUri;
    private String orderId;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_proof);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTxnId = findViewById(R.id.etTxnId);
        ivPreview = findViewById(R.id.ivPreview);
        btnUpload = findViewById(R.id.btnUpload);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnUpload.setOnClickListener(v -> openFileChooser());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(ivPreview);
        }
    }

    private void validateAndSubmit() {
        String txnId = etTxnId.getText().toString().trim();
        
        if (txnId.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Please provide either Transaction ID or Screenshot", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImage(txnId);
        } else {
            saveToDatabase(txnId, null, null);
        }
    }

    private void uploadImage(String txnId) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading Proof...");
        pd.setCancelable(false);
        pd.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("payment_screenshots/" + orderId + "_" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> 
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveToDatabase(txnId, uri.toString(), pd);
            })
        ).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToDatabase(String txnId, String imageUrl, ProgressDialog pd) {
        if (pd == null && imageUrl == null) {
             pd = new ProgressDialog(this);
             pd.setMessage("Submitting...");
             pd.show();
        }
        
        final ProgressDialog finalPd = pd;
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("ORDERS").child(orderId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("transactionId", txnId);
        if (imageUrl != null) {
            updates.put("screenshotUrl", imageUrl);
        }
        updates.put("paymentStatus", "Pending");
        updates.put("proofSubmittedAt", System.currentTimeMillis());

        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (finalPd != null) finalPd.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Proof submitted successfully!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PaymentProofActivity.this, BookingSuccessActivity.class);
                intent.putExtra("bookingId", orderId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show();
            }
        });
    }
}