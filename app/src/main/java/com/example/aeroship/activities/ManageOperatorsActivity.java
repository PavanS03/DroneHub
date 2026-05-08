package com.example.aeroship.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.aeroship.R;
import com.example.aeroship.adapters.OperatorAdminAdapter;
import com.example.aeroship.models.Operator;
import com.google.firebase.database.*;

import java.util.*;

public class ManageOperatorsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private TextView tvCount;

    private LinearLayout tvEmpty;
    private EditText etSearch;

    private OperatorAdminAdapter adapter;
    private List<Operator> list;
    private List<Operator> fullList;

    private DatabaseReference ref;
    private ValueEventListener listener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_manage_operators);

        recycler = findViewById(R.id.recyclerOperators);
        tvCount = findViewById(R.id.tvOperatorCount);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);

        findViewById(R.id.btnAddOperator).setOnClickListener(v -> showAddDialog());

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        list = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new OperatorAdminAdapter(list);

        adapter.setOnItemActionListener(new OperatorAdminAdapter.OnItemActionListener() {
            @Override
            public void onDelete(Operator op) {
                deleteOperator(op);
            }

            @Override
            public void onToggleStatus(Operator op) {
                toggleStatus(op);
            }

            @Override
            public void onItemClick(Operator op) {
                Intent intent = new Intent(ManageOperatorsActivity.this, OperatorMissionsActivity.class);
                intent.putExtra("operatorId", op.getId());
                intent.putExtra("operatorName", op.getName());
                startActivity(intent);
            }
        });

        recycler.setAdapter(adapter);

        ref = FirebaseDatabase.getInstance().getReference("USERS");

        loadOperators();
        setupSearch();
    }


    private void loadOperators() {

        listener = ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                fullList.clear();

                if (!snapshot.exists()) {
                    showEmpty();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Operator op = ds.getValue(Operator.class);
                    if (op != null) {
                        // Ensure IDs are correctly mapped
                        if (op.getUserId() == null || op.getUserId().isEmpty()) {
                            op.setUserId(ds.getKey());
                        }

                        if ("operator".equalsIgnoreCase(op.getRole())) {
                            list.add(op);
                            fullList.add(op);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                tvCount.setText("Total Operators: " + list.size());

                if (list.isEmpty()) {
                    showEmpty();
                } else {
                    hideEmpty();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageOperatorsActivity.this,
                        "Failed to load operators",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showEmpty() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
        recycler.setVisibility(View.GONE);
    }

    private void hideEmpty() {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        recycler.setVisibility(View.VISIBLE);
    }


    private void setupSearch() {

        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {

                String query = s.toString().toLowerCase();

                list.clear();

                for (Operator op : fullList) {

                    if ((op.getName() != null && op.getName().toLowerCase().contains(query)) ||
                            (op.getPhone() != null && op.getPhone().contains(query)) ||
                            (op.getEmail() != null && op.getEmail().toLowerCase().contains(query))) {

                        list.add(op);
                    }
                }

                adapter.notifyDataSetChanged();

                if (list.isEmpty()) {
                    showEmpty();
                } else {
                    hideEmpty();
                }
            }
        });
    }



    private void deleteOperator(Operator op) {

        if (op == null || op.getUserId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Operator")
                .setMessage("Are you sure you want to delete this operator?")
                .setPositiveButton("Delete", (d, w) -> {

                    ref.child(op.getUserId()).removeValue()
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "Operator deleted",
                                        Toast.LENGTH_SHORT).show();

                                list.remove(op);
                                fullList.remove(op);
                                adapter.notifyDataSetChanged();
                                tvCount.setText("Total Operators: " + list.size());
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void toggleStatus(Operator op) {

        if (op == null || op.getUserId() == null) return;

        boolean newActive = !op.isActive();

        ref.child(op.getUserId())
                .child("active")
                .setValue(newActive);

        op.setActive(newActive);
        adapter.notifyDataSetChanged();
    }


    private void showAddDialog() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_operator, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etAltPhone = view.findViewById(R.id.etAltPhone);
        EditText etAge = view.findViewById(R.id.etAge);
        EditText etGender = view.findViewById(R.id.etGender);
        EditText etAddress = view.findViewById(R.id.etAddress);

        builder.setView(view);
        builder.setPositiveButton("Add Operator", null);
        builder.setNegativeButton("Cancel", null);

        dialog = builder.create();
        dialog.show();

        Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String altPhone = etAltPhone.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String gender = etGender.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Basic details (Name, Email, Pass, Phone) are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAdd.setEnabled(false);
            btnAdd.setText("Processing...");

            String uniqueOpId = "OP-" + (System.currentTimeMillis() % 100000);


            com.google.firebase.FirebaseOptions options = com.google.firebase.FirebaseApp.getInstance().getOptions();
            com.google.firebase.FirebaseApp secondaryApp = null;
            try {
                secondaryApp = com.google.firebase.FirebaseApp.getInstance("secondary");
            } catch (IllegalStateException e) {
                secondaryApp = com.google.firebase.FirebaseApp.initializeApp(this, options, "secondary");
            }

            com.google.firebase.auth.FirebaseAuth secondaryAuth = com.google.firebase.auth.FirebaseAuth.getInstance(secondaryApp);

            secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String userId = task.getResult().getUser().getUid();
                        
                        Operator op = new Operator();
                        op.setId(uniqueOpId); // The OP-ID
                        op.setUserId(userId); // The Firebase UID
                        op.setName(name);
                        op.setEmail(email);
                        op.setPassword(password);
                        op.setPhone(phone);
                        op.setAlternateNumber(altPhone);
                        op.setAge(age);
                        op.setGender(gender);
                        op.setAddress(address);
                        op.setRole("operator");
                        op.setStatus("Available");
                        op.setActive(true);
                        op.setCreatedAt(System.currentTimeMillis());

                        ref.child(userId).setValue(op)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Operator Account Created: " + uniqueOpId, Toast.LENGTH_LONG).show();
                                secondaryAuth.signOut();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                btnAdd.setEnabled(true);
                                btnAdd.setText("Add Operator");
                                Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        btnAdd.setEnabled(true);
                        btnAdd.setText("Add Operator");
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        Toast.makeText(this, "Auth Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listener != null) {
            ref.removeEventListener(listener);
        }
    }
}