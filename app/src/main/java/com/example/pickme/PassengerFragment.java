package com.example.pickme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PassengerFragment extends Fragment implements OnMapReadyCallback {

    private static final int FINE_LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText CurrentLocation, NumberOfPassengers, Comments;
    private AutoCompleteTextView Destination;
    private Button btnSubmit;

    // **Added Places API client and adapter**
    private PlacesClient placesClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Places.initialize(requireContext(), "AIzaSyCi5oQJq9geUjJNzICgFOKMz3V-s97wWR8", new Locale("he"));
        placesClient = Places.createClient(requireContext());

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger, container, false);

        CurrentLocation = view.findViewById(R.id.editTextCurrentLocation);
        Destination = view.findViewById(R.id.editTextDestination);
        NumberOfPassengers = view.findViewById(R.id.editTextPassengers);
        Comments = view.findViewById(R.id.editTextComments);

        btnSubmit = view.findViewById(R.id.btnSubmit);

        getCurrentLocationAndDisplayAddress();
        setupDestinationAutocomplete();

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

        btnSubmit.setOnClickListener(v -> savePassengerData());


        return view;
    }

    private void savePassengerData() {
        String currentLoc = CurrentLocation.getText().toString().trim();
        String destination = Destination.getText().toString().trim();
        String numberOfPassengers = NumberOfPassengers.getText().toString().trim();
        String comments = Comments.getText().toString().trim();

        if (currentLoc.isEmpty() || destination.isEmpty() || numberOfPassengers.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userUID = currentUser.getUid();

        // Create a Passenger object
        Passenger passenger = new Passenger(currentLoc, destination, numberOfPassengers, comments);

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

    private void getCurrentLocationAndDisplayAddress() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;

                // Reverse geocode the current location to get the address
                Geocoder geocoder = new Geocoder(requireContext(), new Locale("he"));
                try {
                    List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressLine = address.getAddressLine(0);  // Get the full address line

                        // Set the address and city in the EditText
                        String currentAddress = addressLine;
                        CurrentLocation.setText(currentAddress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error getting address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // **Added method to set up destination autocomplete**
    private void setupDestinationAutocomplete() {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        List<String> suggestions = new ArrayList<>();
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,  
                suggestions
        );
        Destination.setAdapter(destinationAdapter);


        Destination.addTextChangedListener(new TextWatcher() {
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
