package com.example.pickme;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter for displaying a list of available drivers in the PickMe ride-sharing application.
 * This adapter handles the presentation of driver information and manages ride booking functionality
 * through Firebase integration.
 *
 * <p>The adapter creates and manages ViewHolder instances for efficient list display,
 * handles user interactions for booking rides, and integrates with Firebase Database
 * to store passenger booking information under the selected driver's ride data.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Display of driver details including name, age, location, and ride information</li>
 *   <li>Integration with Firebase Authentication for user identification</li>
 *   <li>Firebase Database operations for storing ride bookings</li>
 *   <li>User feedback through Toast messages</li>
 * </ul>
 *
 * @author PickMe Development Team
 * @version 1.0
 * @since 1.0
 */
public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    /**
     * List of Driver objects to be displayed in the RecyclerView.
     * Contains all available drivers with their ride information.
     */
    private List<Driver> driverList;

    /**
     * Android Context used for accessing resources, launching activities,
     * and displaying Toast messages.
     */
    private Context context;

    /**
     * Constructor for DriverAdapter.
     * Initializes the adapter with a list of drivers and the application context.
     *
     * @param driverList the list of Driver objects to display
     * @param context the Android Context for resource access and operations
     */
    public DriverAdapter(List<Driver> driverList, Context context) {
        this.driverList = driverList;
        this.context = context;
    }

    /**
     * Creates a new ViewHolder instance for displaying driver information.
     * This method inflates the item layout and returns a new DriverViewHolder.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View (not used in this implementation)
     * @return a new DriverViewHolder instance containing the inflated item view
     */
    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    /**
     * Binds driver data to the ViewHolder at the specified position.
     * This method populates the ViewHolder's views with driver information
     * and sets up the click listener for the order button.
     *
     * <p>The method handles:</p>
     * <ul>
     *   <li>Setting driver and user information in text views</li>
     *   <li>Configuring the order button click listener</li>
     *   <li>Firebase operations for booking rides</li>
     *   <li>Error handling and user feedback</li>
     * </ul>
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        User user = driver.getUser();

        // Set driver name and age, with fallback for missing user data
        if (user != null) {
            holder.tvNameAge.setText(user.getName() + ", " + user.getAge());
        } else {
            holder.tvNameAge.setText("Unknown Driver");
        }

        // Populate driver information views
        holder.locationTextView.setText("From: " + driver.getCurrentLocation());
        holder.destinationTextView.setText("To: " + driver.getDestination());
        holder.seatsTextView.setText("Seats: " + driver.getNumberOfSeats());
        holder.commentTextView.setText("Comment: " + driver.getComment());
        holder.dateText.setText("Date: " + driver.getDate());
        holder.timeText.setText("Time: " +  driver.getTime());

        // Set up order button click listener for ride booking
        holder.orderButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the ride booking process when the order button is clicked.
             * This method retrieves the current user's passenger data from Firebase
             * and stores it under the selected driver's ride information.
             *
             * @param v the clicked View (order button)
             */
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("Users");
                DatabaseReference ridesRef = database.getReference("Rides");

                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user UID
                String driverUid = driver.getUid(); // Get selected driver UID

                // Retrieve passenger data and book the ride
                usersRef.child(currentUserUid).child("Passenger").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Get passenger data
                        Map<String, Object> passengerData = (Map<String, Object>) task.getResult().getValue();

                        // Save passenger data under Rides/{driverUid}/passengers/{currentUserUid}
                        ridesRef.child(driverUid).child("Passengers").child(currentUserUid).setValue(passengerData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Ride booked successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to book ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "No passenger data found!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Returns the total number of items in the driver list.
     * This method is called by the RecyclerView to determine how many items to display.
     *
     * @return the size of the driver list
     */
    @Override
    public int getItemCount() {
        return driverList.size();
    }

    /**
     * ViewHolder class for caching driver item views.
     * This static inner class holds references to all the views in a driver item layout
     * for efficient recycling and prevents repeated findViewById calls.
     *
     * <p>The ViewHolder contains references to:</p>
     * <ul>
     *   <li>Text views for displaying driver and ride information</li>
     *   <li>Order button for booking rides</li>
     * </ul>
     *
     * @author PickMe Development Team
     * @version 1.0
     * @since 1.0
     */
    static class DriverViewHolder extends RecyclerView.ViewHolder {

        /**
         * TextView for displaying the current location (pickup point).
         */
        TextView locationTextView;

        /**
         * TextView for displaying the destination location.
         */
        TextView destinationTextView;

        /**
         * TextView for displaying the number of available seats.
         */
        TextView seatsTextView;

        /**
         * TextView for displaying the ride date.
         */
        TextView dateText;

        /**
         * TextView for displaying the ride time.
         */
        TextView timeText;

        /**
         * TextView for displaying additional comments about the ride.
         */
        TextView commentTextView;

        /**
         * TextView for displaying the driver's name and age.
         */
        TextView tvNameAge;

        /**
         * Button for booking/ordering the ride with this driver.
         */
        Button orderButton;

        /**
         * Constructor for DriverViewHolder.
         * Initializes all view references by finding them in the provided item view.
         *
         * @param itemView the root view of the driver item layout
         */
        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameAge = itemView.findViewById(R.id.tv_name_age);
            locationTextView = itemView.findViewById(R.id.textLocation);
            destinationTextView = itemView.findViewById(R.id.textDestination);
            seatsTextView = itemView.findViewById(R.id.textSeats);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            commentTextView = itemView.findViewById(R.id.textComment);
            orderButton = itemView.findViewById(R.id.btn_order_driver);
        }
    }
}