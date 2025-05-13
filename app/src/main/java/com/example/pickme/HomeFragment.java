package com.example.pickme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final int FINE_LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private boolean isFirstLocationUpdate = true;

    private PlacesClient placesClient;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    // TextView to display the user's name
    private TextView userNameTextView;

    private EditText loc, set, com;
    private AutoCompleteTextView des;

    private Button btnSub;

    private ImageButton profileButton;

    private ValueEventListener userNameListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Make sure to use your actual API key here
        Places.initialize(requireContext(), "AIzaSyCi5oQJq9geUjJNzICgFOKMz3V-s97wWR8", new Locale("he"));
        placesClient = Places.createClient(requireContext());

        // Initialize location callback for continuous updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = location;

                        // Update the map camera position if needed
                        if (mMap != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            // For first location, add marker and move camera with zoom
                            if (isFirstLocationUpdate) {
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                isFirstLocationUpdate = false;

                                // Update location address in EditText
                                updateLocationAddress(location);
                            } else {
                                // For subsequent updates, just animate camera without changing zoom
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            }
                        }
                    }
                }
            }
        };
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userNameTextView = view.findViewById(R.id.topTextView);
        profileButton = view.findViewById(R.id.profileButton);

        loc = view.findViewById(R.id.loc);
        des = view.findViewById(R.id.des);
        set = view.findViewById(R.id.set);
        com = view.findViewById(R.id.com);
        btnSub = view.findViewById(R.id.btnSub);

        fetchUserName();

        setupDestinationAutocomplete();

        btnSub.setOnClickListener(v -> savePassengersData());

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(5000) // Update interval in milliseconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(2000)
                .build();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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
                            userNameTextView.setText("Welcome, " + userName + "! Need any rides?");
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

    private void savePassengersData() {
        String currentLoc = loc.getText().toString().trim();
        String destination = des.getText().toString().trim();
        String numberOfSeats = set.getText().toString().trim();
        String comments = com.getText().toString().trim();

        if (currentLoc.isEmpty() || destination.isEmpty() || numberOfSeats.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userUID = currentUser.getUid();
        Passenger passenger = new Passenger(currentLoc, destination, numberOfSeats, comments);

        databaseReference.child(userUID).child("Passenger").setValue(passenger).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "Trip details saved successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), PickDriverActivity.class);
                startActivity(intent);

            } else {
                Toast.makeText(requireContext(), "Failed to save trip details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity to prevent back navigation
    }

    private void updateLocationAddress(Location location) {
        // Reverse geocode the current location to get the address
        Geocoder geocoder = new Geocoder(requireContext(), new Locale("he"));
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);  // Get the full address line

                // Set the address in the EditText
                loc.setText(addressLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error getting address", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDestinationAutocomplete() {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        List<String> suggestions = new ArrayList<>();
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                suggestions
        );
        des.setAdapter(destinationAdapter);

        des.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    fetchAutocompleteSuggestions(s.toString(), token, destinationAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchAutocompleteSuggestions(String query, AutocompleteSessionToken token, ArrayAdapter<String> destinationAdapter) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(token)
                .setCountries("IL")
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<String> newSuggestions = new ArrayList<>();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        newSuggestions.add(prediction.getFullText(null).toString());
                    }
                    destinationAdapter.clear();
                    destinationAdapter.addAll(newSuggestions);
                    destinationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Try to apply a default map style if needed (optional)
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            if (!success) {
                // Log error if style couldn't be applied
                System.out.println("Map style parsing failed");
            }
        } catch (Exception e) {
            // If there's no map_style.json or other error, just continue with default style
            e.printStackTrace();
        }

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_REQUEST_CODE);
            return;
        }

        // Enable My Location button and layer
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Set map type to normal to ensure map tiles are displayed
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Start location updates which will handle camera movement
        startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();

                // Enable map location features if map is ready
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show your location on the map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the listener
        if (userNameListener != null && firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            databaseReference.child(userId).child("name").removeEventListener(userNameListener);
        }

        // Make sure to remove location updates
        stopLocationUpdates();
    }
}