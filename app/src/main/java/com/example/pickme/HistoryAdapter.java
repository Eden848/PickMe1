package com.example.pickme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Driver> driverData = new ArrayList<>();
    private List<Passenger> passengerData = new ArrayList<>();
    private boolean showingDriver = true;

    public void setDriverData(List<Driver> data) {
        this.driverData = data;
        showingDriver = true;
        notifyDataSetChanged();
    }

    public void setPassengerData(List<Passenger> data) {
        this.passengerData = data;
        showingDriver = false;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView role, from, to, timeDate;

        public ViewHolder(View view) {
            super(view);
            role = view.findViewById(R.id.tvRole);
            from = view.findViewById(R.id.tvFrom);
            to = view.findViewById(R.id.tvTo);
            timeDate = view.findViewById(R.id.tvTimeDate);
        }
    }

    public void setShowingDriver(boolean isDriver) {
        this.showingDriver = isDriver;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (showingDriver) {
            Driver ride = driverData.get(position);

            holder.role.setText("Driver");
            holder.from.setText("From: " + ride.getCurrentLocation());
            holder.to.setText("To: " + ride.getDestination());
            holder.timeDate.setText("Date: " + ride.getDate() + " " + ride.getTime());

        }else {
            Passenger ride = passengerData.get(position);

            holder.role.setText("Passenger");
            holder.from.setText("From: " + ride.getCurrentLocation());
            holder.to.setText("To: " + ride.getDestination());
            holder.timeDate.setText("Passengers: " + ride.getNumberOfPassengers());
        }
    }

    @Override
    public int getItemCount() {
        return showingDriver ? driverData.size() : passengerData.size();
    }
}