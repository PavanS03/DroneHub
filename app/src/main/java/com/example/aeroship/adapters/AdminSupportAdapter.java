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

public class AdminSupportAdapter
        extends RecyclerView.Adapter<AdminSupportAdapter.ViewHolder> {

    private List<SupportMessage> messageList;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(SupportMessage message);
    }

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public AdminSupportAdapter(List<SupportMessage> messageList) {
        this.messageList = messageList;
    }

    public void setMessages(List<SupportMessage> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_support, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (messageList == null || position < 0 || position >= messageList.size()) return;

        SupportMessage message = messageList.get(position);
        if (message == null) return;

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(message);
            }
        });

        String name = message.getUserName();
        holder.tvUserName.setText(
                (name != null && !name.trim().isEmpty()) ? name : "Customer"
        );

        String phone = message.getUserPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            holder.tvUserPhone.setVisibility(View.VISIBLE);
            holder.tvUserPhone.setText(phone);
        } else {
            holder.tvUserPhone.setVisibility(View.GONE);
        }

        String userMessage = message.getUserMessage();
        holder.tvMessage.setText(
                (userMessage != null && !userMessage.trim().isEmpty())
                        ? userMessage
                        : "No message"
        );

        long timestamp = message.getTimestamp();
        if (timestamp > 0) {
            try {
                holder.tvTime.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                holder.tvTime.setText("");
            }
        } else {
            holder.tvTime.setText("");
        }

        String status = message.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        holder.tvStatus.setText(status);

        if ("Replied".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }

        String reply = message.getAdminReply();

        if (reply != null && !reply.trim().isEmpty()) {
            holder.layoutReply.setVisibility(View.VISIBLE);
            holder.tvAdminReply.setText(reply);
        } else {
            holder.layoutReply.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (messageList != null) ? messageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvUserPhone, tvMessage, tvTime;
        TextView tvStatus, tvAdminReply;
        LinearLayout layoutReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);

            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
            layoutReply = itemView.findViewById(R.id.layoutReply);
        }
    }
}