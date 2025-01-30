package com.example.pickme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final int FINE_LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    // TextView to display the user's name
    private TextView userNameTextView;
    private ImageButton profileButton;

    // Listener for real-time updates
    private ValueEventListener userNameListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userNameTextView = view.findViewById(R.id.topTextView);
        profileButton = view.findViewById(R.id.profileButton);

        fetchUserName();

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Restore the location if it exists
        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getParcelable("currentLocation");
        }

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Fetch the last location if needed
        if (currentLocation == null) {
            getLastLocation();
        }

        return view;
    }

    private void fetchUserName() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Remove any previous listener to avoid duplicates
            if (userNameListener != null) {
                databaseReference.child(userId).child("name").removeEventListener(userNameListener);
            }

            // Define the real-time listener
            userNameListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = snapshot.getValue(String.class);
                        if (userName != null) {
                            userNameTextView.setText("Welcome, " + userName + "!");
                        } else {
                            userNameTextView.setText("Welcome, User!");
                        }
                    } else {
                        userNameTextView.setText("Welcome, User!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            // Attach the listener to the database reference
            databaseReference.child(userId).child("name").addValueEventListener(userNameListener);
        } else {
            // Redirect to login activity if the user is not logged in
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity to prevent back navigation
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable user location if permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Update the map location if we already have it
        if (currentLocation != null) {
            updateMapLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;

                // Update the map immediately if it's ready
                if (mMap != null) {
                    updateMapLocation();
                }
            }
        });
    }

    private void updateMapLocation() {
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current location to the instance state
        outState.putParcelable("currentLocation", currentLocation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && userNameListener != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("name").removeEventListener(userNameListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied, please allow the permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
