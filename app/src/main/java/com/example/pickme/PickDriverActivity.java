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


public class PickDriverActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DriverAdapter driverAdapter;
    private List<Driver> driverList;

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

    private void fetchDrivers() {
        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        ridesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear();
                final int[] loadedDrivers = {0};
                final int totalDrivers = (int) snapshot.getChildrenCount();

                if (totalDrivers == 0) {
                    driverAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Driver driver = rideSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        // Assign UID from Firebase key
                        String userId = rideSnapshot.getKey();
                        driver.setUid(userId);

                        // Add driver to list
                        driverList.add(driver);

                        // Fetch and associate the user
                        if (userId != null && !userId.isEmpty()) {
                            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        String name = userSnapshot.child("name").getValue(String.class);
                                        String age = userSnapshot.child("age").getValue(String.class);

                                        if (name != null) {
                                            User user = new User(name, age != null ? age : "", "");
                                            driver.setUser(user);
                                            Log.d("PickDriverActivity", "User linked to driver: " + name);
                                        }
                                    } else {
                                        Log.d("PickDriverActivity", "No user data found for ID: " + userId);
                                    }

                                    loadedDrivers[0]++;
                                    if (loadedDrivers[0] >= totalDrivers) {
                                        driverAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("FirebaseError", "Error loading user data: " + error.getMessage());
                                    loadedDrivers[0]++;
                                    if (loadedDrivers[0] >= totalDrivers) {
                                        driverAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        } else {
                            loadedDrivers[0]++;
                            if (loadedDrivers[0] >= totalDrivers) {
                                driverAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
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