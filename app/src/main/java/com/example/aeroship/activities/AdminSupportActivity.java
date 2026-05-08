package com.example.aeroship.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.adapters.AdminSupportAdapter;
import com.example.aeroship.models.SupportMessage;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminSupportActivity extends AppCompatActivity {

    private RecyclerView recyclerSupport;
    private TextView tvEmpty;
    private AdminSupportAdapter adapter;
    private List<SupportMessage> messageList;
    private DatabaseReference reference;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support);

        recyclerSupport = findViewById(R.id.recyclerSupport);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerSupport.setLayoutManager(new LinearLayoutManager(this));
        recyclerSupport.setHasFixedSize(true);

        messageList = new ArrayList<>();

        adapter = new AdminSupportAdapter(messageList);
        recyclerSupport.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showReplyDialog);

        reference = FirebaseDatabase.getInstance().getReference("SUPPORT");

        loadMessages();
    }


    private void loadMessages() {

        listener = reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messageList.clear();

                if (!snapshot.exists()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerSupport.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot userNode : snapshot.getChildren()) {

                    String userId = userNode.getKey();

                    for (DataSnapshot msgNode : userNode.getChildren()) {

                        try {
                            if (msgNode.getValue() instanceof String) {
                                continue;
                            }

                            SupportMessage message = msgNode.getValue(SupportMessage.class);

                            if (message != null) {
                                message.setUserId(userId);
                                message.setMessageId(msgNode.getKey());

                                if (message.getUserName() == null ||
                                        message.getUserName().trim().isEmpty()) {
                                    message.setUserName("User");
                                }

                                messageList.add(message);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AdminSupport", "Error parsing message: " + e.getMessage());
                        }
                    }
                }

                Collections.sort(messageList,
                        (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                adapter.setMessages(messageList);

                if (messageList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerSupport.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerSupport.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(AdminSupportActivity.this,
                        "Failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showReplyDialog(SupportMessage message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_reply, null);

        EditText etReply = view.findViewById(R.id.etReply);

        builder.setView(view);
        builder.setTitle("Reply to " + message.getUserName());

        builder.setPositiveButton("Send", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String replyText = etReply.getText() != null
                    ? etReply.getText().toString().trim()
                    : "";

            if (TextUtils.isEmpty(replyText)) {
                etReply.setError("Enter reply");
                return;
            }

            sendReply(message, replyText);
            dialog.dismiss();
        });
    }


    private void sendReply(SupportMessage message, String reply) {

        if (message.getUserId() == null ||
                message.getMessageId() == null) {

            Toast.makeText(this,
                    "Invalid message data",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference msgRef = reference
                .child(message.getUserId())
                .child(message.getMessageId());

        msgRef.child("adminReply").setValue(reply);
        msgRef.child("status").setValue("Replied");

        Toast.makeText(this, "Reply sent", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (reference != null && listener != null) {
            reference.removeEventListener(listener);
        }
    }
}