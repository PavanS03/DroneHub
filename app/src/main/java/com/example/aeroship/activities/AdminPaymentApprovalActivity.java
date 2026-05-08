package com.example.aeroship.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aeroship.R;
import com.example.aeroship.adapters.PaymentAdapter;
import com.example.aeroship.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdminPaymentApprovalActivity extends AppCompatActivity {

    private RecyclerView rvPayments;
    private PaymentAdapter adapter;
    private List<Booking> paymentList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment);

        rvPayments = findViewById(R.id.rvPayments);
        paymentList = new ArrayList<>();
        ordersRef = FirebaseDatabase.getInstance().getReference("ORDERS");

        setupRecyclerView();
        fetchPendingPayments();
    }

    private void setupRecyclerView() {
        adapter = new PaymentAdapter(this, paymentList, new PaymentAdapter.OnPaymentActionListener() {
            @Override
            public void onApprove(Booking booking) {
                updatePaymentStatus(booking, "Approved", "Confirmed");
            }

            @Override
            public void onReject(Booking booking) {
                updatePaymentStatus(booking, "Rejected", "Payment Failed");
            }
        });
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(adapter);
    }

    private void fetchPendingPayments() {
        ordersRef.orderByChild("paymentStatus").equalTo("Pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking booking = ds.getValue(Booking.class);
                    if (booking != null) {
                        if ((booking.getTransactionId() != null && !booking.getTransactionId().isEmpty()) ||
                            (booking.getScreenshotUrl() != null && !booking.getScreenshotUrl().isEmpty())) {
                            paymentList.add(booking);
                        }
                    }
                }
                if (paymentList.isEmpty()) {
                    Toast.makeText(AdminPaymentApprovalActivity.this, "No pending payments found", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPaymentApprovalActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaymentStatus(Booking booking, String payStatus, String orderStatus) {
        DatabaseReference ref = ordersRef.child(booking.getBookingId());
        ref.child("paymentStatus").setValue(payStatus);
        ref.child("status").setValue(orderStatus);
        
        if (payStatus.equals("Approved")) {
            ref.child("advancePaid").setValue(true);
        }

        Toast.makeText(this, "Payment " + payStatus, Toast.LENGTH_SHORT).show();
    }
}