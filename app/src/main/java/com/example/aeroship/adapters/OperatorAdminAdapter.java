package com.example.aeroship.adapters;

import android.app.AlertDialog;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.Operator;
import com.google.firebase.database.*;

import java.util.List;

public class OperatorAdminAdapter extends RecyclerView.Adapter<OperatorAdminAdapter.ViewHolder> {

    private List<Operator> list;

    public interface OnItemActionListener {
        void onDelete(Operator op);
        void onToggleStatus(Operator op);
        void onItemClick(Operator op);
    }

    private OnItemActionListener listener;

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public OperatorAdminAdapter(List<Operator> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v) {

        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_operator_admin, p, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {

        Operator op = list.get(i);
        if (op == null) return;

        h.tvOpId.setText(op.getId());
        h.tvName.setText(op.getName());
        h.tvPhone.setText("Phone: " + op.getPhone() + (op.getAlternateNumber().isEmpty() ? "" : " / " + op.getAlternateNumber()));
        h.tvEmail.setText("Email: " + op.getEmail());
        h.tvAddress.setText("Address: " + op.getAddress());

        DatabaseReference ordersRef =
                FirebaseDatabase.getInstance().getReference("ORDERS");

        ordersRef.orderByChild("assignedOperatorId")
                .equalTo(op.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean isBusy = false;

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String status = ds.child("status").getValue(String.class);

                            if (status != null &&
                                    (status.equalsIgnoreCase("Assigned")
                                            || status.equalsIgnoreCase("In Progress"))) {

                                isBusy = true;
                                break;
                            }
                        }

                        if (isBusy) {
                            h.tvStatus.setText("Busy");
                            h.tvStatus.setBackgroundResource(R.drawable.bg_status_busy);
                        } else {
                            h.tvStatus.setText("Available");
                            h.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        h.switchActive.setOnCheckedChangeListener(null);
        h.switchActive.setChecked(op.isActive());

        h.switchActive.setOnCheckedChangeListener((b, isChecked) -> {

            if (listener != null) {
                listener.onToggleStatus(op);
            }

            Toast.makeText(h.itemView.getContext(),
                    isChecked ? "Activated" : "Deactivated",
                    Toast.LENGTH_SHORT).show();
        });

        if (op.isActive()) {
            h.btnDisable.setText("Disable");
            h.btnDisable.setBackgroundColor(h.itemView.getContext().getResources().getColor(R.color.error_red));
        } else {
            h.btnDisable.setText("Enable");
            h.btnDisable.setBackgroundColor(h.itemView.getContext().getResources().getColor(R.color.success_green));
        }

        h.btnDisable.setOnClickListener(v -> {
            boolean newStatus = !op.isActive();
            op.setActive(newStatus);
            
            FirebaseDatabase.getInstance().getReference("USERS")
                    .child(op.getUserId())
                    .child("isActive")
                    .setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        notifyItemChanged(h.getAdapterPosition());
                        Toast.makeText(h.itemView.getContext(), 
                            newStatus ? "Operator Enabled" : "Operator Disabled", 
                            Toast.LENGTH_SHORT).show();
                    });
        });

        h.btnAssign.setOnClickListener(v -> {
            Toast.makeText(h.itemView.getContext(), "Opening Assignment Panel...", Toast.LENGTH_SHORT).show();
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(op);
            }
        });

        h.itemView.setOnLongClickListener(v -> {

            new AlertDialog.Builder(h.itemView.getContext())
                    .setTitle("Delete Operator")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Delete", (d, w) -> {
                        if (listener != null) {
                            listener.onDelete(op);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        h.tvStatus.setOnClickListener(v -> {

            Toast.makeText(
                    h.itemView.getContext(),
                    "Status is auto-managed by system",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone, tvEmail, tvAddress, tvStatus, tvOpId;
        Switch switchActive;
        Button btnDisable, btnAssign;

        public ViewHolder(View v) {
            super(v);

            tvOpId = v.findViewById(R.id.tvOpId);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvStatus = v.findViewById(R.id.tvStatus);
            switchActive = v.findViewById(R.id.switchActive);
            btnDisable = v.findViewById(R.id.btnDisable);
            btnAssign = v.findViewById(R.id.btnAssign);
        }
    }
}