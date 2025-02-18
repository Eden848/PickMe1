package com.example.pickme;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder>{

    private List<Driver> driverList;

    public DriverAdapter(List<Driver> driverList) {
        this.driverList = driverList;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        User user = driver.getUser();

        if (user != null) {
            holder.tvNameAge.setText(user.getName() + ", " + user.getAge());
        } else {
            holder.tvNameAge.setText("Unknown Driver");
        }

        holder.locationTextView.setText("From: " + driver.getCurrentLocation());
        holder.destinationTextView.setText("To: " + driver.getDestination());
        holder.seatsTextView.setText("Seats: " + driver.getNumberOfSeats());
        holder.commentTextView.setText("Comment: " + driver.getComment());
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView, destinationTextView, seatsTextView, commentTextView, tvNameAge;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameAge = itemView.findViewById(R.id.tv_name_age);
            locationTextView = itemView.findViewById(R.id.textLocation);
            destinationTextView = itemView.findViewById(R.id.textDestination);
            seatsTextView = itemView.findViewById(R.id.textSeats);
            commentTextView = itemView.findViewById(R.id.textComment);
        }
    }

}
