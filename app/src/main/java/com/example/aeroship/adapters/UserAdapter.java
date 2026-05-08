package com.example.aeroship.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> userList;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private OnUserClickListener listener;

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = userList.get(position);

        if (user == null) return;

        String name = user.getName() != null ? user.getName() : "No Name";
        String email = user.getEmail() != null ? user.getEmail() : "No Email";
        String phone = user.getPhone() != null ? user.getPhone() : "No Phone";
        String password = user.getPassword() != null ? user.getPassword() : "N/A";
        String role = user.getRole() != null ? user.getRole() : "user";

        holder.tvName.setText(name);
        holder.tvEmail.setText("Email: " + email);
        holder.tvPhone.setText("Phone: " + phone);

        if (holder.tvPassword != null) {
            holder.tvPassword.setText("Password: " + password);
        }

        if (holder.tvRole != null) {

            holder.tvRole.setText(role.toUpperCase());

            if (role.equalsIgnoreCase("admin")) {
                holder.tvRole.setBackgroundResource(R.drawable.bg_status_busy);
            } else if (role.equalsIgnoreCase("operator")) {
                holder.tvRole.setBackgroundResource(R.drawable.bg_status_pending);
            } else {
                holder.tvRole.setBackgroundResource(R.drawable.bg_status_available);
            }
        }

        if (holder.tvStatus != null) {

            if (user.isActive()) {
                holder.tvStatus.setText("Active");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
            } else {
                holder.tvStatus.setText("Inactive");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_busy);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(80f);

        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvPhone;

        TextView tvPassword, tvRole, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);

            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvRole = itemView.findViewById(R.id.tvRole);

            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}