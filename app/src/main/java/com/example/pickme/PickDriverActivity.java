package com.example.pickme;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code PickDriverActivity} is an Android Activity responsible for displaying a list of available drivers
 * (rides) to the user. It fetches ride data from Firebase Realtime Database, populates a
 * {@link RecyclerView} with this data, and associates driver information with user details.
 * Users can then select a driver from this list.
 */
public class PickDriverActivity extends AppCompatActivity {

    /**
     * RecyclerView to display the list of available drivers.
     */
    private RecyclerView recyclerView;
    /**
     * Adapter for the RecyclerView, responsible for binding driver data to list items.
     */
    private DriverAdapter driverAdapter;
    /**
     * List to hold {@link Driver} objects fetched from Firebase.
     */
    private List<Driver> driverList;

    /**
     * Called when the activity is first created. This method initializes the UI components,
     * sets up the RecyclerView with a LinearLayoutManager and a {@link DriverAdapter},
     * and initiates the process of fetching driver data from Firebase.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_driver);

        recyclerView = findViewById(R.id.recyclerViewDrivers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driverList, this);
        recyclerView.setAdapter(driverAdapter);

        fetchDrivers();
    }

    /**
     * Fetches available driver (ride) data from the Firebase Realtime Database.
     * It listens for a single event on the "Rides" node, then iterates through each ride
     * to extract driver information. For each driver, it also fetches associated user
     * details (like name and age) from the "Users" node to enrich the driver object.
     * The RecyclerView adapter is notified once all driver and user data is loaded.
     */
    private void fetchDrivers() {
        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        ridesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear(); // Clear existing list before populating
                final int[] loadedDrivers = {0}; // Counter for loaded drivers to track completion
                final int totalDrivers = (int) snapshot.getChildrenCount(); // Total number of rides (potential drivers)

                // If no rides are available, update the adapter and return
                if (totalDrivers == 0) {
                    driverAdapter.notifyDataSetChanged();
                    return;
                }

                // Iterate through each ride snapshot to extract driver data
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Driver driver = rideSnapshot.getValue(Driver.class); // Convert snapshot to Driver object
                    if (driver != null) {
                        // Assign UID from Firebase key to the driver object
                        String userId = rideSnapshot.getKey();
                        driver.setUid(userId);

                        // Add driver to the temporary list
                        driverList.add(driver);

                        // Fetch and associate the user details (name, age) for this driver
                        if (userId != null && !userId.isEmpty()) {
                            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        String name = userSnapshot.child("name").getValue(String.class);
                                        String age = userSnapshot.child("age").getValue(String.class);

                                        if (name != null) {
                                            // Create a User object and link it to the driver
                                            User user = new User(name, age != null ? age : "", "");
                                            driver.setUser(user);
                                            Log.d("PickDriverActivity", "User linked to driver: " + name);
                                        }
                                    } else {
                                        Log.d("PickDriverActivity", "No user data found for ID: " + userId);
                                    }

                                    // Increment loaded drivers count and notify adapter if all data is loaded
                                    loadedDrivers[0]++;
                                    if (loadedDrivers[0] >= totalDrivers) {
                                        driverAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("FirebaseError", "Error loading user data: " + error.getMessage());
                                    // Increment count even on error to ensure notifyDataSetChanged is called
                                    loadedDrivers[0]++;
                                    if (loadedDrivers[0] >= totalDrivers) {
                                        driverAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        } else {
                            // If userId is null or empty, still increment count
                            loadedDrivers[0]++;
                            if (loadedDrivers[0] >= totalDrivers) {
                                driverAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        // If driver object is null, still increment count
                        loadedDrivers[0]++;
                        if (loadedDrivers[0] >= totalDrivers) {
                            driverAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading rides: " + error.getMessage());
                Toast.makeText(PickDriverActivity.this, "Failed to load drivers.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
