package com.example.pickme;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass that handles the display and interaction
 * for both drivers and passengers regarding active rides. This fragment dynamically
 * updates its UI based on the user's role (driver or passenger) and the status
 * of their active ride in the Firebase Realtime Database.
 *
 * <p>It manages UI elements such as driver and passenger information cards,
 * ride status displays, and action buttons (e.g., cancel ride, accept/decline passenger,
 * start/end ride).</p>
 */
public class PassengerFragment extends Fragment {

    // UI components from XML
    /**
     * TextView displayed when there are no active rides for the current user.
     */
    private TextView noActiveRidesText;

    // Shared ride container
    /**
     * RelativeLayout container for the shared ride information, visible when a ride is active.
     */
    private RelativeLayout sharedRideContainer;
    /**
     * LinearLayout container for the active ride details, holding driver and passenger info cards.
     */
    private LinearLayout activeRideContainer;

    // Driver information card and its elements
    /**
     * CardView displaying the driver's ride information.
     */
    private CardView driverInfoCard;
    /**
     * TextView for displaying the driver's name.
     */
    private TextView driverName;
    /**
     * TextView for displaying the driver's starting location.
     */
    private TextView driverFrom;
    /**
     * TextView for displaying the driver's destination.
     */
    private TextView driverTo;
    /**
     * TextView for displaying the ride's scheduled time.
     */
    private TextView driverTime;
    /**
     * TextView for displaying the number of seats offered by the driver.
     */
    private TextView driverSeats;
    /**
     * TextView for displaying any additional comments from the driver.
     */
    private TextView driverComment;

    // Passenger information card and its elements
    /**
     * CardView displaying the passenger's ride request information.
     */
    private CardView passengerInfoCard;
    /**
     * TextView displayed when there is no passenger information to show (e.g., driver with no accepted passenger).
     */
    private TextView noPassengerInfo;
    /**
     * LinearLayout container for displaying detailed passenger information.
     */
    private LinearLayout passengerDetailsContainer;
    /**
     * TextView for displaying the passenger's name.
     */
    private TextView passengerName;
    /**
     * TextView for displaying the passenger's pickup location.
     */
    private TextView passengerFrom;
    /**
     * TextView for displaying the passenger's drop-off destination.
     */
    private TextView passengerTo;
    /**
     * TextView for displaying the number of seats requested by the passenger.
     */
    private TextView passengerSeats;
    /**
     * TextView for displaying any additional comments from the passenger.
     */
    private TextView passengerComment;

    // Driver UI elements
    /**
     * LinearLayout container for driver-specific control buttons.
     */
    private LinearLayout driverControls;
    /**
     * Button for the driver to cancel their ride when no passenger is accepted.
     */
    private Button driverCancelRideButton;
    /**
     * LinearLayout container for driver controls related to passenger management (accept/decline/start/end).
     */
    private LinearLayout driverPassengerControls;
    /**
     * Button for the driver to accept a pending passenger request.
     */
    private Button acceptPassengerButton;
    /**
     * Button for the driver to decline a pending passenger request.
     */
    private Button declineButton;

    // Passenger UI elements
    /**
     * Button for the passenger to cancel their ride request.
     */
    private Button passengerCancelButton;
    /**
     * TextView displaying the current status of the passenger's ride (e.g., "Waiting for confirmation..", "Driver is on the way!").
     */
    private TextView passengerStatus;
    /**
     * Button for the driver to start the ride. Dynamically created and added to UI.
     */
    private Button startRideButton;
    /**
     * Button for the driver to end the ride. Dynamically created and added to UI.
     */
    private Button endRideButton;

    // Firebase references
    /**
     * DatabaseReference to the "Rides" node in Firebase, which stores all active ride information.
     */
    private DatabaseReference ridesReference;
    /**
     * DatabaseReference to the "Users" node in Firebase, used to retrieve user details like names.
     */
    private DatabaseReference usersReference;
    /**
     * The unique ID of the currently authenticated user.
     */
    private String userId;
    /**
     * A boolean flag indicating whether the current user is a driver with an active ride.
     */
    private boolean isDriver = false;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is where UI components are initialized and Firebase references are set up.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger, container, false);

        // Initialize views according to XML
        noActiveRidesText = view.findViewById(R.id.no_active_rides);

        // Shared container
        sharedRideContainer = view.findViewById(R.id.shared_ride_container);
        activeRideContainer = view.findViewById(R.id.active_ride_container);

        // Driver info card
        driverInfoCard = view.findViewById(R.id.driver_info_card);
        driverName = view.findViewById(R.id.driver_name);
        driverFrom = view.findViewById(R.id.driver_from);
        driverTo = view.findViewById(R.id.driver_to);
        driverTime = view.findViewById(R.id.driver_time);
        driverSeats = view.findViewById(R.id.driver_seats);
        driverComment = view.findViewById(R.id.driver_comment);

        // Passenger info card
        passengerInfoCard = view.findViewById(R.id.passenger_info_card);
        noPassengerInfo = view.findViewById(R.id.no_passenger_info);
        passengerDetailsContainer = view.findViewById(R.id.passenger_details_container);
        passengerName = view.findViewById(R.id.passenger_name);
        passengerFrom = view.findViewById(R.id.passenger_from);
        passengerTo = view.findViewById(R.id.passenger_to);
        passengerSeats = view.findViewById(R.id.passenger_seats);
        passengerComment = view.findViewById(R.id.passenger_comment);

        // Driver controls
        driverControls = view.findViewById(R.id.driver_controls);
        driverCancelRideButton = view.findViewById(R.id.driver_cancel_ride_button);
        driverPassengerControls = view.findViewById(R.id.driver_passenger_controls);
        acceptPassengerButton = view.findViewById(R.id.accept_passenger_button);
        declineButton = view.findViewById(R.id.decline_button);

        // Passenger controls
        passengerCancelButton = view.findViewById(R.id.passenger_cancel_button);
        passengerStatus = view.findViewById(R.id.passenger_status);

        // Initialize start ride button
        startRideButton = new Button(getContext());
        startRideButton.setText("Start Ride");
        startRideButton.setBackgroundTintList(acceptPassengerButton.getBackgroundTintList());
        startRideButton.setTextColor(acceptPassengerButton.getTextColors());
        startRideButton.setPadding(
                acceptPassengerButton.getPaddingLeft(),
                acceptPassengerButton.getPaddingTop(),
                acceptPassengerButton.getPaddingRight(),
                acceptPassengerButton.getPaddingBottom()
        );
        startRideButton.setTextSize(16);
        startRideButton.setLayoutParams(acceptPassengerButton.getLayoutParams());

        // Initialize end ride button
        endRideButton = new Button(getContext());
        endRideButton.setText("End Ride");
        endRideButton.setBackgroundTintList(acceptPassengerButton.getBackgroundTintList());
        endRideButton.setTextColor(acceptPassengerButton.getTextColors());
        endRideButton.setPadding(
                acceptPassengerButton.getPaddingLeft(),
                acceptPassengerButton.getPaddingTop(),
                acceptPassengerButton.getPaddingRight(),
                acceptPassengerButton.getPaddingBottom()
        );
        endRideButton.setTextSize(16);
        endRideButton.setLayoutParams(acceptPassengerButton.getLayoutParams());

        // Initialize Firebase references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ridesReference = FirebaseDatabase.getInstance().getReference("Rides");
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        // Set up button click listeners
        setupButtonListeners();

        // Load user role and data
        loadUserRole();

        return view;
    }

    /**
     * Sets up all the click listeners for the various buttons in the fragment.
     * This includes buttons for drivers (cancel ride, accept/decline passenger, start/end ride)
     * and passengers (cancel ride).
     */
    private void setupButtonListeners() {
        // Driver's cancel ride button (when no passenger)
        driverCancelRideButton.setOnClickListener(v -> {
            // Implementation for driver canceling ride
            Toast.makeText(getContext(), "Ride canceled by driver", Toast.LENGTH_SHORT).show();
            deleteRide();
        });

        // Accept passenger button listener
        acceptPassengerButton.setOnClickListener(v -> {
            // Implementation for accepting passenger
            Toast.makeText(getContext(), "Passenger accepted", Toast.LENGTH_SHORT).show();

            // Update Firebase data to reflect passenger acceptance
            updateRideStatus("accepted");

            // Replace "Accept Passenger" button with "Start Ride" button
            LinearLayout parentLayout = (LinearLayout) acceptPassengerButton.getParent();
            int index = parentLayout.indexOfChild(acceptPassengerButton);
            parentLayout.removeView(acceptPassengerButton);
            parentLayout.addView(startRideButton, index);
        });

        // Start ride button click listener
        startRideButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ride started", Toast.LENGTH_SHORT).show();
            updateRideStatus("in_progress");

            // Hide decline button
            declineButton.setVisibility(View.GONE);

            // Replace start ride button with end ride button
            LinearLayout parentLayout = (LinearLayout) startRideButton.getParent();
            int index = parentLayout.indexOfChild(startRideButton);
            parentLayout.removeView(startRideButton);
            parentLayout.addView(endRideButton, index);
        });

        // End ride button click listener
        endRideButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ride ended", Toast.LENGTH_SHORT).show();
            updateRideStatus("completed");

            DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
            DatabaseReference historyDriverRef = FirebaseDatabase.getInstance().getReference("HistoryDriver");
            DatabaseReference historyPassengerRef = FirebaseDatabase.getInstance().getReference("HistoryPassenger");

            String driverUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            ridesRef.child(driverUID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Step 1: Extract ONLY driver data (exclude "Passengers")
                        Map<String, Object> driverData = new HashMap<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (!child.getKey().equals("Passengers")) {
                                driverData.put(child.getKey(), child.getValue());
                            }
                        }

                        String rideId = FirebaseDatabase.getInstance().getReference().push().getKey();

// Save driver data under HistoryDriver/driverUID/rideId
                        historyDriverRef.child(driverUID).child(rideId).setValue(driverData)
                                .addOnSuccessListener(aVoid -> {
                                    // Handle passengers
                                    DataSnapshot passengersSnap = snapshot.child("Passengers");
                                    if (passengersSnap.exists()) {
                                        for (DataSnapshot passengerEntry : passengersSnap.getChildren()) {
                                            String passengerUID = passengerEntry.getKey();
                                            Object passengerData = passengerEntry.getValue();

                                            // Save under HistoryPassenger/passengerUID/rideId
                                            historyPassengerRef.child(passengerUID).child(rideId).setValue(passengerData)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        // Remove ride only once after all is copied (optional)
                                                        ridesRef.child(driverUID).removeValue();
                                                        Toast.makeText(getContext(), "Ride saved with ID " + rideId, Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("Firebase", "Passenger save failed: " + e.getMessage());
                                                    });
                                        }
                                    } else {
                                        ridesRef.child(driverUID).removeValue();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Driver save failed: " + e.getMessage());
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "DB Error: " + error.getMessage());
                }
            });

            deleteRide();
        });

        // Decline button listener
        declineButton.setOnClickListener(v -> {
            // Implementation for declining passenger
            Toast.makeText(getContext(), "Passenger declined", Toast.LENGTH_SHORT).show();

            // Get the first passenger's ID
            ridesReference.child(userId).child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        // For simplicity, get the first passenger
                        String passengerUid = snapshot.getChildren().iterator().next().getKey();

                        // Remove this passenger from the ride
                        ridesReference.child(userId).child("Passengers").child(passengerUid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Reset the ride status to "waiting"
                                    ridesReference.child(userId).child("status").setValue("waiting")
                                            .addOnSuccessListener(statusVoid -> {
                                                Toast.makeText(getContext(), "Passenger removed and status reset", Toast.LENGTH_SHORT).show();

                                                // Refresh the driver UI to show waiting for passengers
                                                ridesReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot rideSnapshot) {
                                                        if (rideSnapshot.exists()) {
                                                            showDriverUI(rideSnapshot);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(getContext(), "Failed to refresh ride data", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to reset ride status: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to remove passenger: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to access passenger data", Toast.LENGTH_SHORT).show();
                }
            });

        });

        // Cancel ride button listener for passenger
        passengerCancelButton.setOnClickListener(v -> {
            // Implementation for canceling ride
            Toast.makeText(getContext(), "Ride canceled by passenger", Toast.LENGTH_SHORT).show();
            // Remove passenger from ride in Firebase
            removePassengerFromRide();
        });
    }

    /**
     * Deletes the current user's active ride from the "Rides" node in Firebase.
     * This method is typically called when a driver cancels their ride.
     */
    private void deleteRide() {
        if (userId != null) {
            ridesReference.child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Ride deleted successfully", Toast.LENGTH_SHORT).show();
                        // Refresh UI
                        showNoActiveRides();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to delete ride: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Updates the status of the current user's active ride in Firebase.
     * This method is used by the driver to change the ride status (e.g., "accepted", "in_progress", "completed").
     *
     * @param status The new status string to set for the ride.
     */
    private void updateRideStatus(String status) {
        // Update ride status in Firebase
        if (userId != null) {
            ridesReference.child(userId).child("status").setValue(status)
                    .addOnSuccessListener(aVoid -> {
                        // Success handling
                        if (status.equals("accepted")) {
                            Toast.makeText(getContext(), "Passenger accepted successfully", Toast.LENGTH_SHORT).show();
                        } else if (status.equals("in_progress")) {
                            Toast.makeText(getContext(), "Ride started successfully", Toast.LENGTH_SHORT).show();
                        } else if (status.equals("completed")) {
                            Toast.makeText(getContext(), "Ride completed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error handling
                        Toast.makeText(getContext(), "Failed to update ride status: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Removes the current user (as a passenger) from any active ride they are part of in Firebase.
     * This method iterates through all active rides to find the one where the current user is a passenger,
     * removes their entry, and then resets the ride's status to "waiting" if it was previously "accepted".
     */
    private void removePassengerFromRide() {
        // Find which ride the passenger is part of
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    String driverUid = rideSnapshot.getKey();
                    DataSnapshot passengersSnapshot = rideSnapshot.child("Passengers");

                    if (passengersSnapshot.hasChild(userId)) {
                        // Remove passenger from ride
                        ridesReference.child(driverUid).child("Passengers").child(userId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Reset the ride status to "waiting"
                                    ridesReference.child(driverUid).child("status").setValue("waiting")
                                            .addOnSuccessListener(statusVoid -> {
                                                Toast.makeText(getContext(), "Ride canceled and status reset", Toast.LENGTH_SHORT).show();
                                                // Refresh UI
                                                showNoActiveRides();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to reset ride status: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to cancel ride: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to access ride data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Determines the current user's role (driver or passenger) by checking Firebase.
     * It first checks if the user has an active ride as a driver. If not, it then
     * searches if the user is listed as a passenger in any active ride.
     * Based on the role, it calls the appropriate UI display method.
     */
    private void loadUserRole() {
        // First check if user is a driver with an active ride
        ridesReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isDriver = true;
                    showDriverUI(snapshot);
                } else {
                    // Not a driver, check if user is a passenger on any ride
                    findPassengerRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load ride data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Searches through all active rides in Firebase to determine if the current user
     * is listed as a passenger in any of them. If a ride is found, it updates the UI
     * to the passenger view. If no ride is found, it displays the "no active rides" message.
     */
    private void findPassengerRide() {
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean foundRide = false;

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    DataSnapshot passengersSnapshot = rideSnapshot.child("Passengers");
                    if (passengersSnapshot.hasChild(userId)) {
                        isDriver = false;
                        showPassengerUI(rideSnapshot);
                        foundRide = true;
                        break;
                    }
                }

                if (!foundRide) {
                    // Show no active rides message
                    showNoActiveRides();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load passenger data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Configures the UI to display the "no active rides" message.
     * It hides the shared ride container and makes the `noActiveRidesText` visible.
     */
    private void showNoActiveRides() {
        // Show message if no rides available
        noActiveRidesText.setVisibility(View.VISIBLE);
        sharedRideContainer.setVisibility(View.GONE);
    }

    /**
     * Configures the UI for a passenger view. This involves showing the shared ride container,
     * hiding driver controls, showing passenger controls, and populating the driver and
     * passenger information cards with relevant data from the provided `rideSnapshot`.
     * It also updates the passenger's ride status.
     *
     * @param rideSnapshot The DataSnapshot containing the details of the ride the passenger is part of.
     */
    private void showPassengerUI(DataSnapshot rideSnapshot) {
        // Hide no rides message
        noActiveRidesText.setVisibility(View.GONE);

        // Show shared container
        sharedRideContainer.setVisibility(View.VISIBLE);

        // Configure UI for passenger view - rearrange cards to show passenger first
        reorderCardsForPassenger();

        // Configure controls
        driverControls.setVisibility(View.GONE);
        passengerCancelButton.setVisibility(View.VISIBLE);

        // Show status for passenger
        passengerStatus.setVisibility(View.VISIBLE);

        // Get passenger details from ride
        String passengerDestination = null;
        String passengerLocation = null;
        String seats = null;
        String comment = null;

        DataSnapshot passengerDataSnapshot = rideSnapshot.child("Passengers").child(userId);
        if (passengerDataSnapshot.exists()) {
            passengerDestination = passengerDataSnapshot.child("destination").getValue(String.class);
            passengerLocation = passengerDataSnapshot.child("currentLocation").getValue(String.class);
            seats = passengerDataSnapshot.child("numberOfPassengers").getValue(String.class);
            comment = passengerDataSnapshot.child("comment").getValue(String.class);
        }

        // Set passenger info details
        noPassengerInfo.setVisibility(View.GONE);
        passengerDetailsContainer.setVisibility(View.VISIBLE);

        passengerFrom.setText("From: " + (passengerLocation != null ? passengerLocation : "Not specified"));
        passengerTo.setText("To: " + (passengerDestination != null ? passengerDestination : "Not specified"));
        passengerSeats.setText("Seats requested: " + (seats != null ? seats : "Not specified"));
        passengerComment.setText(comment != null ? comment : "No comment");

        // Load passenger name
        // This method is assumed to exist elsewhere or needs to be implemented.
        // loadUserInfo(userId, "Passenger: ", passengerName);

        // Get ride details
        String destination = rideSnapshot.child("destination").getValue(String.class);
        String currentLocation = rideSnapshot.child("currentLocation").getValue(String.class);
        String time = rideSnapshot.child("time").getValue(String.class);
        String driverSeatsAvailable = rideSnapshot.child("numberOfSeats").getValue(String.class);
        String driverCommentText = rideSnapshot.child("comment").getValue(String.class);

        // Set driver ride details
        driverFrom.setText("From: " + (currentLocation != null ? currentLocation : "Not specified"));
        driverTo.setText("To: " + (destination != null ? destination : "Not specified"));
        driverTime.setText(time != null ? time : "Time not specified");
        driverSeats.setText("Available seats: " + (driverSeatsAvailable != null ? driverSeatsAvailable : "Not specified"));
        driverComment.setText(driverCommentText != null ? driverCommentText : "No comment");

        String rideStatus = rideSnapshot.child("status").getValue(String.class);
        if (rideStatus != null && rideStatus.equals("accepted")) {
            passengerStatus.setText("Driver is on the way!");
            passengerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (rideStatus != null && rideStatus.equals("in_progress")) {
            passengerStatus.setText("Ride in progress");
            passengerStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            // Hide cancel button once ride is in progress
            passengerCancelButton.setVisibility(View.GONE);
        } else {
            passengerStatus.setText("Waiting for confirmation..");
            passengerStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Load driver info
        String driverUid = rideSnapshot.getKey();
        if (driverUid != null) {
            // This method is assumed to exist elsewhere or needs to be implemented.
            // loadUserInfo(driverUid, "Driver: ", driverName);
        }
    }

    /**
     * Configures the UI for a driver view. This involves showing the shared ride container,
     * showing driver controls, hiding passenger controls, and populating the driver and
     * passenger (if any) information cards with relevant data from the provided `rideSnapshot`.
     * It also dynamically adjusts driver control buttons based on the ride status and presence of passengers.
     *
     * @param rideSnapshot The DataSnapshot containing the details of the driver's active ride.
     */
    private void showDriverUI(DataSnapshot rideSnapshot) {
        // Hide no rides message
        noActiveRidesText.setVisibility(View.GONE);

        // Show shared container
        sharedRideContainer.setVisibility(View.VISIBLE);

        // Configure UI for driver view - rearrange cards to show driver first
        reorderCardsForDriver();

        // Configure controls
        driverControls.setVisibility(View.VISIBLE);
        passengerCancelButton.setVisibility(View.GONE);

        // Hide status for driver
        passengerStatus.setVisibility(View.GONE);

        // Get ride details
        String destination = rideSnapshot.child("destination").getValue(String.class);
        String currentLocation = rideSnapshot.child("currentLocation").getValue(String.class);
        String time = rideSnapshot.child("time").getValue(String.class);
        String seats = rideSnapshot.child("numberOfSeats").getValue(String.class);
        String comment = rideSnapshot.child("comment").getValue(String.class);
        String rideStatus = rideSnapshot.child("status").getValue(String.class);

        // Set driver info details
        driverFrom.setText("From: " + (currentLocation != null ? currentLocation : "Not specified"));
        driverTo.setText("To: " + (destination != null ? destination : "Not specified"));
        driverTime.setText(time != null ? time : "Time not specified");
        driverSeats.setText("Available seats: " + (seats != null ? seats : "Not specified"));
        driverComment.setText(comment != null ? comment : "No comment");

        // Load driver name (self info)
        // This method is assumed to exist elsewhere or needs to be implemented.
        // loadUserInfo(userId, "Driver: ", driverName);

        // Check if there are passengers
        DataSnapshot passengersSnapshot = rideSnapshot.child("Passengers");
        if (passengersSnapshot.exists() && passengersSnapshot.getChildrenCount() > 0) {
            // There are passengers
            noPassengerInfo.setVisibility(View.GONE);
            passengerDetailsContainer.setVisibility(View.VISIBLE);

            // For simplicity, we'll just display the first passenger if there are multiple
            DataSnapshot firstPassenger = passengersSnapshot.getChildren().iterator().next();
            String passengerUid = firstPassenger.getKey();

            if (passengerUid != null) {
                // Load passenger name
                // This method is assumed to exist elsewhere or needs to be implemented.
                // loadUserInfo(passengerUid, "Passenger: ", passengerName);

                // Get passenger details
                String passengerDestination = firstPassenger.child("destination").getValue(String.class);
                String passengerLocation = firstPassenger.child("currentLocation").getValue(String.class);
                String passengerSeatsRequested = firstPassenger.child("numberOfPassengers").getValue(String.class);
                String passengerCommentText = firstPassenger.child("comment").getValue(String.class);

                // Set passenger details
                passengerFrom.setText("From: " + (passengerLocation != null ? passengerLocation : "Not specified"));
                passengerTo.setText("To: " + (passengerDestination != null ? passengerDestination : "Not specified"));
                passengerSeats.setText("Seats requested: " + (passengerSeatsRequested != null ? passengerSeatsRequested : "1"));
                passengerComment.setText(passengerCommentText != null ? passengerCommentText : "No comment");
            }

            // Show appropriate controls based on ride status
            driverCancelRideButton.setVisibility(View.GONE);

            if (rideStatus != null && rideStatus.equals("accepted")) {
                // Show the startRideButton and hide acceptPassengerButton
                driverPassengerControls.setVisibility(View.VISIBLE);
                driverPassengerControls.removeView(acceptPassengerButton);
                if (driverPassengerControls.indexOfChild(startRideButton) == -1) {
                    driverPassengerControls.addView(startRideButton, 0);
                }
            } else if (rideStatus != null && rideStatus.equals("in_progress")) {
                // Show the endRideButton and hide decline button
                driverPassengerControls.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.GONE);
                driverPassengerControls.removeView(startRideButton);
                if (driverPassengerControls.indexOfChild(endRideButton) == -1) {
                    driverPassengerControls.addView(endRideButton, 0);
                }
            } else {
                // Default: show accept/decline buttons
                driverPassengerControls.setVisibility(View.VISIBLE);
                if (driverPassengerControls.indexOfChild(acceptPassengerButton) == -1) {
                    driverPassengerControls.addView(acceptPassengerButton, 0);
                }
                declineButton.setVisibility(View.VISIBLE);
            }
        } else {
            // No passengers yet
            noPassengerInfo.setVisibility(View.VISIBLE);
            passengerDetailsContainer.setVisibility(View.GONE);

            // Show only cancel button when no passenger
            driverCancelRideButton.setVisibility(View.VISIBLE);
            driverPassengerControls.setVisibility(View.GONE);
        }
    }

    // Method to reorder cards for passenger view (passenger card first)
    /**
     * Reorders the CardViews within the `activeRideContainer` to display the
     * passenger information card before the driver information card.
     * This is typically done when the current user is a passenger.
     */
    private void reorderCardsForPassenger() {
        if (activeRideContainer != null) {
            // Remove both cards
            activeRideContainer.removeView(driverInfoCard);
            activeRideContainer.removeView(passengerInfoCard);

            // Add passenger card first, then driver card
            activeRideContainer.addView(passengerInfoCard, 0);
            activeRideContainer.addView(driverInfoCard, 1);
        }
    }

    // Method to reorder cards for driver view (driver card first)
    /**
     * Reorders the CardViews within the `activeRideContainer` to display the
     * driver information card before the passenger information card.
     * This is typically done when the current user is a driver.
     */
    private void reorderCardsForDriver() {
        if (activeRideContainer != null) {
            // Remove both cards to re-add them in the desired order
            activeRideContainer.removeView(passengerInfoCard);
            activeRideContainer.removeView(driverInfoCard);

            // Add driver card first (index 0), then passenger card (index 1)
            activeRideContainer.addView(driverInfoCard, 0);
            activeRideContainer.addView(passengerInfoCard, 1);
        }
    }
}
