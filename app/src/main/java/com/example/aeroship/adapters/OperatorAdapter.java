package com.example.aeroship.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final Context context;
    private final OperatorActionListener listener;

    public interface OperatorActionListener {
        void onAccept(Booking booking);
        void onFinish(Booking booking);
    }

    public OperatorAdapter(List<Booking> bookingList, OperatorActionListener listener) {
        this.bookingList = bookingList;
        this.context = (Context) listener;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operator_booking, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Booking booking = bookingList.get(position);
        if (booking == null) return;

        String status = booking.getStatus() != null ? booking.getStatus() : "Assigned";

        holder.tvBookingInfo.setText(
                "👤 Customer: " + booking.getUserName() +
                        "\n📍 Location: " + booking.getLocation() +
                        "\n📅 Date: " + booking.getDate() +
                        "\n🚁 Sector: " + booking.getSector() +
                        "\n📞 Phone: " + booking.getUserPhone() +
                        "\n\nStatus: " + status
        );

        Boolean advancePaid = booking.getAdvancePaid();

        if (advancePaid == null || !advancePaid) {
            holder.btnAssignDrone.setText("Waiting Payment");
            holder.btnAssignDrone.setEnabled(false);
            return;
        }

        switch (status) {

            case "Assigned":

                holder.btnAssignDrone.setVisibility(View.VISIBLE);
                holder.btnAssignDrone.setEnabled(true);
                holder.btnAssignDrone.setText("Accept");

                holder.btnAssignDrone.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAccept(booking);
                    }
                });

                break;

            case "In Progress":

                holder.btnAssignDrone.setVisibility(View.VISIBLE);
                holder.btnAssignDrone.setEnabled(true);
                holder.btnAssignDrone.setText("Finish");

                holder.btnAssignDrone.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFinish(booking);
                    }
                });

                break;

            case "Completed":

                holder.btnAssignDrone.setText("Completed");
                holder.btnAssignDrone.setEnabled(false);
                break;

            default:

                holder.btnAssignDrone.setText("Unavailable");
                holder.btnAssignDrone.setEnabled(false);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList == null ? 0 : bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBookingInfo;
        MaterialButton btnAssignDrone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBookingInfo = itemView.findViewById(R.id.tvBookingInfo);
            btnAssignDrone = itemView.findViewById(R.id.btnAssignDrone);
        }
    }
}