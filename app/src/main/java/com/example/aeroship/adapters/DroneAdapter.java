package com.example.aeroship.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aeroship.R;
import com.example.aeroship.models.DronePackage;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class DroneAdapter extends RecyclerView.Adapter<DroneAdapter.DroneViewHolder> {

    private Context context;
    private List<DronePackage> list;
    private DroneActionListener listener;

    public interface DroneActionListener {
        void onEdit(DronePackage drone);
        void onDelete(DronePackage drone);
        void onToggle(DronePackage drone);
    }

    public DroneAdapter(List<DronePackage> list, DroneActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DroneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_drone_admin, parent, false);
        return new DroneViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DroneViewHolder holder, int position) {
        DronePackage drone = list.get(position);
        holder.tvId.setText(drone.getPackageId());
        holder.tvSector.setText(drone.getSectorId());
        holder.tvLevelRate.setText("Level: " + drone.getLevel() + " | Rate: ₹" + drone.getPricePerHour() + "/hr");
        holder.tvStatus.setText(drone.isAvailable() ? "Active" : "Inactive");

        if (drone.isAvailable()) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_available);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_busy);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(drone));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(drone));
        holder.itemView.setOnClickListener(v -> listener.onToggle(drone));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class DroneViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvSector, tvLevelRate, tvStatus;
        MaterialButton btnEdit, btnDelete;

        public DroneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvDroneId);
            tvSector = itemView.findViewById(R.id.tvSector);
            tvLevelRate = itemView.findViewById(R.id.tvLevelRate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
