package com.example.aeroship.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.AdminPaymentAdapter;
import com.example.aeroship.models.Payment;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminPaymentsActivity extends AppCompatActivity {

    private RecyclerView recyclerPayments;
    private List<Payment> paymentList;
    private AdminPaymentAdapter adapter;
    private DatabaseReference paymentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payments);

        recyclerPayments = findViewById(R.id.recyclerPayments);
        recyclerPayments.setLayoutManager(
                new LinearLayoutManager(this));

        paymentList = new ArrayList<>();
        adapter = new AdminPaymentAdapter(paymentList);
        recyclerPayments.setAdapter(adapter);

        paymentRef = FirebaseDatabase
                .getInstance()
                .getReference("PAYMENTS");

        loadPayments();
    }

    private void loadPayments() {

        paymentRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        paymentList.clear();

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Payment payment =
                                    ds.getValue(Payment.class);

                            if (payment != null) {
                                payment.setPaymentId(ds.getKey());
                                paymentList.add(payment);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {}
                });
    }
}