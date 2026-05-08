package com.example.aeroship.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.Sector;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SectorAdapter
        extends RecyclerView.Adapter<SectorAdapter.SectorViewHolder> {

    public interface OnSectorClickListener {
        void onSectorClick(Sector sector);
    }

    private final List<Sector> sectorList;
    private final OnSectorClickListener listener;

    public SectorAdapter(List<Sector> sectorList,
                         OnSectorClickListener listener) {
        this.sectorList = sectorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectorViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sector_user, parent, false);

        return new SectorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SectorViewHolder holder,
            int position) {

        Sector sector = sectorList.get(position);

        holder.tvName.setText(sector.getName());

        int resId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(
                        sector.getImageUrl(),
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

        if (resId != 0) {
            holder.imgSector.setImageResource(resId);
        } else {
            holder.imgSector.setImageResource(R.drawable.logo_aeroship);
        }

        if (sector.isActive()) {

            holder.card.setAlpha(1f);
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(Color.parseColor("#00C853"));

        } else if (sector.isPaused()) {

            holder.card.setAlpha(0.5f);
            holder.tvStatus.setText("Paused");
            holder.tvStatus.setTextColor(Color.parseColor("#FFA000"));

        } else {

            holder.card.setAlpha(0.3f);
            holder.tvStatus.setText("Deleted");
            holder.tvStatus.setTextColor(Color.RED);
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null && sector.isActive()) {
                listener.onSectorClick(sector);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sectorList.size();
    }

    static class SectorViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        ImageView imgSector;
        TextView tvName, tvStatus;

        public SectorViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.cardSector);
            imgSector = itemView.findViewById(R.id.imgSector);
            tvName = itemView.findViewById(R.id.tvSectorName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}