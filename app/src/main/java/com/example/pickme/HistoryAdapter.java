package com.example.pickme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying ride history items in the PickMe application.
 * This adapter can handle both driver and passenger ride history data and switches
 * between them based on the current display mode.
 *
 * <p>The adapter supports displaying different types of ride information:
 * <ul>
 *   <li>Driver rides: Shows role, pickup location, destination, date and time</li>
 *   <li>Passenger rides: Shows role, pickup location, destination, and number of passengers</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @since 1.0
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /** List containing driver ride history data */
    private List<Driver> driverData = new ArrayList<>();

    /** List containing passenger ride history data */
    private List<Passenger> passengerData = new ArrayList<>();

    /** Flag indicating whether driver data is currently being displayed */
    private boolean showingDriver = true;

    /**
     * Sets the driver ride history data and switches the adapter to display driver mode.
     * This method will notify the adapter that the data set has changed.
     *
     * @param data List of Driver objects containing ride history information
     */
    public void setDriverData(List<Driver> data) {
        this.driverData = data;
        showingDriver = true;
        notifyDataSetChanged();
    }

    /**
     * Sets the passenger ride history data and switches the adapter to display passenger mode.
     * This method will notify the adapter that the data set has changed.
     *
     * @param data List of Passenger objects containing ride history information
     */
    public void setPassengerData(List<Passenger> data) {
        this.passengerData = data;
        showingDriver = false;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for caching view references in ride history items.
     * This improves performance by avoiding repeated findViewById calls.
     *
     * <p>Each ViewHolder contains references to:
     * <ul>
     *   <li>role: TextView displaying user role (Driver/Passenger)</li>
     *   <li>from: TextView displaying pickup location</li>
     *   <li>to: TextView displaying destination</li>
     *   <li>timeDate: TextView displaying date/time or passenger count</li>
     * </ul>
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView for displaying the user role (Driver or Passenger) */
        TextView role;

        /** TextView for displaying the pickup location */
        TextView from;

        /** TextView for displaying the destination */
        TextView to;

        /** TextView for displaying date/time information or passenger count */
        TextView timeDate;

        /**
         * Constructor for ViewHolder that initializes view references.
         *
         * @param view The item view containing the TextViews to be cached
         */
        public ViewHolder(View view) {
            super(view);
            role = view.findViewById(R.id.tvRole);
            from = view.findViewById(R.id.tvFrom);
            to = view.findViewById(R.id.tvTo);
            timeDate = view.findViewById(R.id.tvTimeDate);
        }
    }

    /**
     * Sets the display mode for the adapter.
     * When set to true, displays driver ride history; when false, displays passenger history.
     * This method will notify the adapter that the data set has changed.
     *
     * @param isDriver true to show driver data, false to show passenger data
     */
    public void setShowingDriver(boolean isDriver) {
        this.showingDriver = isDriver;
        notifyDataSetChanged();
    }

    /**
     * Creates new ViewHolder instances when needed by the RecyclerView.
     * Inflates the ride_history_item layout for each list item.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (not used in this implementation)
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     * The binding behavior changes based on whether driver or passenger data is being displayed.
     *
     * <p>For driver data:
     * <ul>
     *   <li>Sets role to "Driver"</li>
     *   <li>Displays current location and destination</li>
     *   <li>Shows date and time information</li>
     * </ul>
     *
     * <p>For passenger data:
     * <ul>
     *   <li>Sets role to "Passenger"</li>
     *   <li>Displays current location and destination</li>
     *   <li>Shows number of passengers instead of date/time</li>
     * </ul>
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (showingDriver) {
            Driver ride = driverData.get(position);

            holder.role.setText("Driver");
            holder.from.setText("From: " + ride.getCurrentLocation());
            holder.to.setText("To: " + ride.getDestination());
            holder.timeDate.setText("Date: " + ride.getDate() + " " + ride.getTime());

        } else {
            Passenger ride = passengerData.get(position);

            holder.role.setText("Passenger");
            holder.from.setText("From: " + ride.getCurrentLocation());
            holder.to.setText("To: " + ride.getDestination());
            holder.timeDate.setText("Passengers: " + ride.getNumberOfPassengers());
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * The count depends on which data set is currently being displayed.
     *
     * @return The total number of items in the currently active data set
     */
    @Override
    public int getItemCount() {
        return showingDriver ? driverData.size() : passengerData.size();
    }
}