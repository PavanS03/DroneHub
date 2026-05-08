package com.example.aeroship.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.SupportMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserSupportAdapter extends RecyclerView.Adapter<UserSupportAdapter.ViewHolder> {

    private List<SupportMessage> messageList;

    public UserSupportAdapter(List<SupportMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_support, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SupportMessage message = messageList.get(position);

        if (message == null) return;

        holder.tvUserMessage.setText(
                message.getUserMessage() != null
                        ? message.getUserMessage()
                        : ""
        );

        if (message.getAdminReply() != null &&
                !message.getAdminReply().trim().isEmpty()) {

            holder.layoutAdminReply.setVisibility(View.VISIBLE);
            holder.tvAdminReply.setText(message.getAdminReply());

        } else {
            holder.layoutAdminReply.setVisibility(View.GONE);
        }

        String status = message.getStatus() != null
                ? message.getStatus()
                : "Pending";

        holder.tvStatus.setText(status);

        if (status.equalsIgnoreCase("Replied")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }

        try {
            long time = message.getTimestamp();
            String formattedTime = new SimpleDateFormat("hh:mm a",
                    Locale.getDefault()).format(new Date(time));

            holder.tvTime.setText(formattedTime);

        } catch (Exception e) {
            holder.tvTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserMessage, tvAdminReply, tvStatus, tvTime;
        LinearLayout layoutAdminReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
            layoutAdminReply = itemView.findViewById(R.id.layoutAdminReply);

            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}