package com.example.pickme;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying ride history in the PickMe application.
 * This fragment allows users to view their ride history as both a driver and passenger
 * by providing toggle buttons and loading data from Firebase Realtime Database.
 *
 * <p>The fragment includes:
 * <ul>
 *   <li>Two toggle buttons for switching between driver and passenger history</li>
 *   <li>A RecyclerView for displaying ride history items</li>
 *   <li>Firebase integration for loading historical data</li>
 *   <li>Error handling with Toast messages</li>
 * </ul>
 *
 * <p>Data is fetched from Firebase database paths:
 * <ul>
 *   <li>Driver history: "HistoryDriver/{userId}"</li>
 *   <li>Passenger history: "HistoryPassenger/{userId}"</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @since 1.0
 */
public class HistoryFragment extends Fragment {

    /** Button for switching to driver history view */
    private Button btnDriverHistory;

    /** Button for switching to passenger history view */
    private Button btnPassengerHistory;

    /** RecyclerView for displaying the list of ride history items */
    private RecyclerView recyclerView;

    /** Adapter for managing ride history data display in RecyclerView */
    private HistoryAdapter adapter;

    /** List containing driver ride history data */
    private List<Driver> driverList;

    /** List containing passenger ride history data */
    private List<Passenger> passengerList;

    /** Flag indicating whether driver history is currently being displayed */
    private boolean showingDriver = true;

    /** Current authenticated user's unique identifier from Firebase Auth */
    private String currentUserUid;

    /**
     * Required empty public constructor for Fragment instantiation.
     * The Android system uses this constructor when recreating fragments.
     */
    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is created. Initializes user authentication
     * and prepares data lists for ride history.
     *
     * <p>This method:
     * <ul>
     *   <li>Gets the current user's UID from Firebase Authentication</li>
     *   <li>Initializes empty ArrayLists for driver and passenger data</li>
     * </ul>
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                          this is the state. This value may be null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverList = new ArrayList<>();
        passengerList = new ArrayList<>();
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes UI components, sets up the RecyclerView with adapter,
     * and configures button click listeners.
     *
     * <p>This method performs the following setup:
     * <ul>
     *   <li>Inflates the fragment layout</li>
     *   <li>Initializes UI components (buttons and RecyclerView)</li>
     *   <li>Sets up RecyclerView with LinearLayoutManager and HistoryAdapter</li>
     *   <li>Configures click listeners for history toggle buttons</li>
     *   <li>Loads default driver history data</li>
     * </ul>
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        btnDriverHistory = view.findViewById(R.id.btnDriverHistory);
        btnPassengerHistory = view.findViewById(R.id.btnPassengerHistory);
        recyclerView = view.findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter();
        adapter.setDriverData(driverList);      // when showing driver history
        adapter.setPassengerData(passengerList); // when showing passenger history

        recyclerView.setAdapter(adapter);

        btnDriverHistory.setOnClickListener(v -> {
            showingDriver = true;
            loadDriverHistory();
        });

        btnPassengerHistory.setOnClickListener(v -> {
            showingDriver = false;
            loadPassengerHistory();
        });

        // Load default
        loadDriverHistory();

        return view;
    }

    /**
     * Loads driver ride history data from Firebase Realtime Database.
     * Clears existing driver data and fetches fresh data from the "HistoryDriver" node
     * for the current authenticated user.
     *
     * <p>This method:
     * <ul>
     *   <li>Clears the current driver list to avoid duplicates</li>
     *   <li>Queries Firebase database at "HistoryDriver/{currentUserUid}"</li>
     *   <li>Deserializes each ride snapshot into Driver objects</li>
     *   <li>Updates the adapter to show driver data</li>
     *   <li>Handles database errors with Toast notifications</li>
     * </ul>
     *
     * <p>Uses Firebase's {@code addListenerForSingleValueEvent} to fetch data once
     * rather than maintaining a persistent listener.
     */
    private void loadDriverHistory() {
        driverList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("HistoryDriver").child(currentUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * Called when data is successfully retrieved from Firebase.
             * Processes each ride snapshot and adds it to the driver list,
             * then updates the adapter to display the new data.
             *
             * @param snapshot DataSnapshot containing all driver ride history for the user
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Driver driver = rideSnapshot.getValue(Driver.class);
                    driverList.add(driver);
                }
                adapter.setShowingDriver(true);
                adapter.notifyDataSetChanged();
            }

            /**
             * Called when the Firebase operation fails.
             * Displays an error message to the user via Toast.
             *
             * @param error DatabaseError containing error details
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load driver history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads passenger ride history data from Firebase Realtime Database.
     * Clears existing passenger data and fetches fresh data from the "HistoryPassenger" node
     * for the current authenticated user.
     *
     * <p>This method:
     * <ul>
     *   <li>Clears the current passenger list to avoid duplicates</li>
     *   <li>Queries Firebase database at "HistoryPassenger/{currentUserUid}"</li>
     *   <li>Deserializes each ride snapshot into Passenger objects</li>
     *   <li>Updates the adapter to show passenger data</li>
     *   <li>Handles database errors with Toast notifications</li>
     * </ul>
     *
     * <p>Uses Firebase's {@code addListenerForSingleValueEvent} to fetch data once
     * rather than maintaining a persistent listener.
     */
    private void loadPassengerHistory() {
        passengerList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("HistoryPassenger").child(currentUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * Called when data is successfully retrieved from Firebase.
             * Processes each ride snapshot and adds it to the passenger list,
             * then updates the adapter to display the new data.
             *
             * @param snapshot DataSnapshot containing all passenger ride history for the user
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Passenger passenger = rideSnapshot.getValue(Passenger.class);
                    passengerList.add(passenger);
                }
                adapter.setShowingDriver(false);
                adapter.notifyDataSetChanged();
            }

            /**
             * Called when the Firebase operation fails.
             * Displays an error message to the user via Toast.
             *
             * @param error DatabaseError containing error details
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load passenger history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}