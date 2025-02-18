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
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_driver);

        recyclerView = findViewById(R.id.recyclerViewDrivers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driverList);
        recyclerView.setAdapter(driverAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchDrivers();
    }

    private void fetchDrivers() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue(String.class);
                    String userAge = userSnapshot.child("age").getValue(String.class);

                    DataSnapshot driverSnapshot = userSnapshot.child("Driver");

                    if (driverSnapshot.exists() && userName != null && userAge != null) {
                        String location = driverSnapshot.child("currentLocation").getValue(String.class);
                        String destination = driverSnapshot.child("destination").getValue(String.class);
                        String seats = driverSnapshot.child("numberOfSeats").getValue(String.class);
                        String comment = driverSnapshot.child("comment").getValue(String.class);

                        // Create driver object and attach user details
                        Driver driver = new Driver(location, destination, seats, comment);
                        driver.setUser(new User(userName, userAge, ""));

                        driverList.add(driver);
                    }
                }

                driverAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PickDriverActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });
    }

}
