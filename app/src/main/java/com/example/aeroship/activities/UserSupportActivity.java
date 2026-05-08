package com.example.aeroship.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.UserSupportAdapter;
import com.example.aeroship.models.SupportMessage;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserSupportActivity extends AppCompatActivity {

    private EditText etMessage;
    private MaterialButton btnSend;
    private RecyclerView recyclerMessages;

    private final List<SupportMessage> messageList = new ArrayList<>();
    private UserSupportAdapter adapter;

    private DatabaseReference reference, usersRef;
    private FirebaseAuth auth;

    private String userId;
    private String userName = "Customer";
    private String userPhone = "";

    private boolean isUserLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_support);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = auth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference("SUPPORT");
        usersRef = FirebaseDatabase.getInstance().getReference("USERS");

        setupRecycler();
        fetchUserDetails();
        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setHasFixedSize(true);

        adapter = new UserSupportAdapter(messageList);
        recyclerMessages.setAdapter(adapter);
    }

    private void fetchUserDetails() {
        usersRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);

                            if (name != null && !name.trim().isEmpty()) {
                                userName = name;
                            }

                            if (phone != null && !phone.trim().isEmpty()) {
                                userPhone = phone;
                            }
                        }

                        isUserLoaded = true;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        isUserLoaded = true;
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText() != null
                ? etMessage.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);

        usersRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);

                        if (name == null || name.trim().isEmpty()) {
                            name = userName != null ? userName : "Customer";
                        }

                        if (phone == null || phone.trim().isEmpty()) {
                            phone = userPhone != null ? userPhone : "";
                        }

                        String messageId = UUID.randomUUID().toString();

                        SupportMessage supportMessage = new SupportMessage();
                        supportMessage.setMessageId(messageId);
                        supportMessage.setUserId(userId);
                        supportMessage.setUserName(name);
                        supportMessage.setUserPhone(phone);
                        supportMessage.setUserMessage(messageText);
                        supportMessage.setAdminReply("");
                        supportMessage.setTimestamp(System.currentTimeMillis());
                        supportMessage.setStatus("Pending");

                        reference.child(userId)
                                .child(messageId)
                                .setValue(supportMessage)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(UserSupportActivity.this,
                                            "Message sent",
                                            Toast.LENGTH_SHORT).show();

                                    etMessage.setText("");
                                    btnSend.setEnabled(true);

                                    recyclerMessages.post(() ->
                                            recyclerMessages.scrollToPosition(messageList.size())
                                    );
                                })
                                .addOnFailureListener(e -> {
                                    btnSend.setEnabled(true);
                                    Toast.makeText(UserSupportActivity.this,
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        btnSend.setEnabled(true);

                        Toast.makeText(UserSupportActivity.this,
                                "Failed to fetch user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        reference.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                if (ds.getValue() instanceof String) {
                                    continue; 
                                }

                                SupportMessage msg = ds.getValue(SupportMessage.class);

                                if (msg != null) {
                                    messageList.add(msg);
                                }
                            } catch (Exception e) {
                            }
                        }

                        Collections.sort(messageList,
                                (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

                        adapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            recyclerMessages.post(() ->
                                    recyclerMessages.scrollToPosition(messageList.size() - 1)
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(UserSupportActivity.this,
                                "Failed to load messages",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}