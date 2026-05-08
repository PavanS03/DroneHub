package com.example.aeroship.adapters;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aeroship.R;
import com.example.aeroship.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class OperatorBookingAdapter extends RecyclerView.Adapter<OperatorBookingAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final Context context;
    private final DatabaseReference reference;
    private final String operatorId;

    public OperatorBookingAdapter(List<Booking> bookingList, Context context) {

        this.bookingList = bookingList;
        this.context = context;
        this.reference = FirebaseDatabase.getInstance().getReference("ORDERS");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            operatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            operatorId = "";
        }
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

        String bookingId = booking.getBookingId();
        String status = booking.getStatus() != null ? booking.getStatus() : "Pending";
        String operatorFromBooking = booking.getOperatorId();

        holder.tvBookingInfo.setText(
                "Sector: " + booking.getSector() +
                        "\nLevel: " + booking.getLevel() +
                        "\nDate: " + booking.getDate() +
                        "\nStatus: " + status
        );

        holder.btnAssignDrone.setEnabled(true);

        switch (status) {

            case "Approved":

                holder.btnAssignDrone.setText("Accept Booking");

                holder.btnAssignDrone.setOnClickListener(v -> {

                    reference.child(bookingId).child("status").setValue("Accepted");
                    reference.child(bookingId).child("operatorId").setValue(operatorId);

                    Toast.makeText(context, "Booking Accepted", Toast.LENGTH_SHORT).show();
                });
                break;

            case "Accepted":

                if (operatorId.equals(operatorFromBooking)) {

                    holder.btnAssignDrone.setText("Start Work");

                    holder.btnAssignDrone.setOnClickListener(v -> {
                        reference.child(bookingId).child("status").setValue("On The Way");
                        Toast.makeText(context, "Work Started", Toast.LENGTH_SHORT).show();
                    });

                } else {
                    holder.btnAssignDrone.setText("Assigned to another operator");
                    holder.btnAssignDrone.setEnabled(false);
                }
                break;

            case "On The Way":

                if (operatorId.equals(operatorFromBooking)) {

                    holder.btnAssignDrone.setText("Mark Completed");

                    holder.btnAssignDrone.setOnClickListener(v -> {
                        reference.child(bookingId).child("status").setValue("Completed");
                        Toast.makeText(context, "Job Completed", Toast.LENGTH_SHORT).show();
                    });

                } else {
                    holder.btnAssignDrone.setText("In Progress");
                    holder.btnAssignDrone.setEnabled(false);
                }
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