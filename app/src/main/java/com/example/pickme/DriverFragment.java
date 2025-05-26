package com.example.pickme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

/**
 * Fragment that handles driver functionality in the PickMe ride-sharing application.
 * This fragment allows users to create and publish ride offers by providing their
 * current location, destination, available seats, and scheduling information.
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Automatic current location detection using GPS and FusedLocationProviderClient</li>
 *   <li>Google Places API integration for destination autocomplete suggestions</li>
 *   <li>Interactive Google Maps display with location markers</li>
 *   <li>Date and time picker dialogs for ride scheduling</li>
 *   <li>Firebase Authentication and Database integration</li>
 *   <li>Dynamic UI updates and ride summary display</li>
 *   <li>Location permission handling</li>
 * </ul>
 *
 * <p>The fragment implements OnMapReadyCallback to handle Google Maps initialization
 * and provides a comprehensive interface for drivers to create ride offers.</p>
 *
 * @author PickMe Development Team
 * @version 1.0
 * @since 1.0
 */
public class DriverFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Request code for fine location permission requests.
     */
    private static final int FINE_LOCATION_REQUEST_CODE = 1;

    /**
     * Google Maps instance for displaying location information.
     */
    private GoogleMap gMap;

    /**
     * Current location of the device obtained through GPS.
     */
    private Location currentLocation;

    /**
     * Client for accessing device location services.
     */
    private FusedLocationProviderClient fusedLocationProviderClient;

    /**
     * Firebase Authentication instance for user management.
     */
    private FirebaseAuth firebaseAuth;

    /**
     * Firebase Database reference pointing to the "Rides" node.
     */
    private DatabaseReference databaseReference;

    /**
     * EditText for displaying and editing the current location.
     */
    private EditText editTextCurrentLocation;

    /**
     * EditText for inputting the number of available seats.
     */
    private EditText editTextsSeats;

    /**
     * EditText for additional comments about the ride.
     */
    private EditText editTextComments;

    /**
     * AutoCompleteTextView for destination input with autocomplete suggestions.
     */
    private AutoCompleteTextView editTextDestination;

    /**
     * Button for submitting the ride information.
     */
    private Button btnSubmit;

    /**
     * Button for navigation to other activities.
     */
    private Button navigateButton;

    /**
     * RelativeLayout container for dynamic content updates.
     */
    private RelativeLayout relativeLayout;

    /**
     * Google Places API client for location autocomplete functionality.
     */
    private PlacesClient placesClient;

    /**
     * TextView for displaying ride summary information.
     */
    private TextView summaryTextView;

    /**
     * TextView for displaying the selected date.
     */
    private TextView tvSelectedDate;

    /**
     * TextView for displaying the selected time.
     */
    private TextView tvSelectedTime;

    /**
     * Calendar instance for date and time operations.
     */
    private Calendar calendar;

    /**
     * Container layout for dynamic UI content replacement.
     */
    private RelativeLayout containerLayout;

    /**
     * Called when the fragment is first created.
     * Initializes location services, Firebase instances, and Google Places API.
     *
     * @param savedInstanceState the saved instance state bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Rides");

        Places.initialize(requireContext(), "AIzaSyCi5oQJq9geUjJNzICgFOKMz3V-s97wWR8", new Locale("he"));
        placesClient = Places.createClient(requireContext());
    }

    /**
     * Creates and initializes the fragment's view hierarchy.
     * Sets up all UI components, event listeners, and initializes map and location services.
     *
     * @param inflater the LayoutInflater object used to inflate views
     * @param container the parent view that the fragment's UI will be attached to
     * @param savedInstanceState the saved instance state bundle
     * @return the root view of the fragment's layout
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver, container, false);

        // Initialize UI components
        editTextCurrentLocation = view.findViewById(R.id.editTextCurrentLocation);
        editTextDestination = view.findViewById(R.id.editTextDestination);
        editTextsSeats = view.findViewById(R.id.editTextsSeats);
        editTextComments = view.findViewById(R.id.editTextComments);

        summaryTextView = view.findViewById(R.id.summaryTextView);
        containerLayout = view.findViewById(R.id. roundedBackground);

        btnSubmit = view.findViewById(R.id.btnSubmit);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);

        calendar = Calendar.getInstance();

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

        btnSubmit.setOnClickListener(v -> saveDriversData());

        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        tvSelectedTime.setOnClickListener(v -> showTimePicker());

        return view;
    }

    /**
     * Displays a date picker dialog for selecting the ride date.
     * Updates the selected date TextView with the chosen date.
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    tvSelectedDate.setText("Date: " + selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog for selecting the ride time.
     * Updates the selected time TextView with the chosen time in 24-hour format.
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    tvSelectedTime.setText("Time: " + selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    /**
     * Replaces the current layout content with a ride summary display.
     * Creates a dynamic layout showing driver information, ride details,
     * and a publish button for finalizing the ride offer.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Removes existing views from the container</li>
     *   <li>Fetches driver information from Firebase</li>
     *   <li>Creates a formatted summary of ride details</li>
     *   <li>Adds navigation functionality to PickDriverActivity</li>
     * </ul>
     */
    private void replaceLayoutContent() {
        // Remove all views in the container
        containerLayout.removeAllViews();

        // Fetch the text from the EditText fields
        String currentLoc = editTextCurrentLocation.getText().toString().trim();
        String destination = editTextDestination.getText().toString().trim();
        String numberOfSeats = editTextsSeats.getText().toString().trim();
        String comments = editTextComments.getText().toString().trim();

        // Create a summary string
        String summaryText = "Current Location: " + currentLoc + "\n" + "\n" +
                "Destination: " + destination + "\n" + "\n" +
                "Seats: " + numberOfSeats + "\n" + "\n" +
                "Comments: " + comments;

        // Create a LinearLayout to contain both TextView and Button
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL); // Stack text and button vertically
        linearLayout.setPadding(20, 20, 20, 20); // Padding for the container
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a LinearLayout for horizontal layout (icon + user name)
        LinearLayout userInfoLayout = new LinearLayout(requireContext());
        userInfoLayout.setOrientation(LinearLayout.HORIZONTAL); // Stack icon and text horizontally
        userInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create an ImageView for the account icon
        ImageView accountIcon = new ImageView(requireContext());
        accountIcon.setImageResource(R.drawable.icon_account_circle); // Set your account icon here
        // Set the icon size to wrap_content to adjust to text size
        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        accountIcon.setLayoutParams(iconLayoutParams);

        // Create a TextView to display the user's name at the top
        TextView userNameTextView = new TextView(requireContext());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("Firebase", "Fetching name for user: " + userId);

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("name");
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getValue(String.class);
                    if (userName != null) {
                        Log.d("Firebase", "User name: " + userName);
                        // Set the user's name in the TextView
                        userNameTextView.setText(" Driver: " + userName);
                        userNameTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color black
                        userNameTextView.setTextSize(22); // Increased text size for user name
                        userNameTextView.setPadding(0, 0, 0, 16); // Padding to space the text from the summary

                        // Add the ImageView (icon) and TextView (user name) to the horizontal LinearLayout
                        userInfoLayout.addView(accountIcon); // Add icon
                        userInfoLayout.addView(userNameTextView); // Add name

                        // Add the user info layout (icon + name) to the main linear layout
                        linearLayout.addView(userInfoLayout);

                        // Now create the summary text view after the name is fetched
                        TextView newSummaryTextView = new TextView(requireContext());
                        newSummaryTextView.setText(summaryText); // Set the summary text
                        newSummaryTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color black
                        newSummaryTextView.setTextSize(18); // Keep text size for summary
                        newSummaryTextView.setPadding(0, 16, 0, 50); // Add space between name and summary
                        newSummaryTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        // Add the TextView to the LinearLayout
                        linearLayout.addView(newSummaryTextView);

                        // Create the Button to navigate to PickDriverActivity
                        Button navigateButton = new Button(requireContext());
                        navigateButton.setId(R.id.navigateButton); // Assign an ID to the button
                        navigateButton.setText("Publish!");
                        navigateButton.setTextColor(Color.WHITE); // Set text color to white
                        navigateButton.setTextSize(18);
                        navigateButton.setPadding(16, 16, 16, 16); // Padding for the button

                        // Prevent the text from being capitalized
                        navigateButton.setAllCaps(false); // This disables uppercase text

                        // Apply the custom drawable as the button's background (with rounded corners)
                        navigateButton.setBackgroundResource(R.drawable.rounded_corner); // Set rounded background
                        navigateButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A2A2A"))); // Set background color

                        // Set the OnClickListener for the Button
                        navigateButton.setOnClickListener(v -> {
                            // Intent to navigate to PickDriverActivity
                            Intent intent = new Intent(requireContext(), PickDriverActivity.class);
                            startActivity(intent);
                        });

                        // Add the Button to the LinearLayout
                        linearLayout.addView(navigateButton);

                        // Add the LinearLayout to the RelativeLayout (container)
                        containerLayout.addView(linearLayout);
                    } else {
                        Log.e("Firebase", "User name not found in database");
                    }
                } else {
                    Log.e("Firebase", "Error fetching user name: " + task.getException());
                }
            });
        } else {
            Log.e("Firebase", "User is not authenticated");
        }
    }

    /**
     * Saves the driver's ride data to Firebase Database.
     * Validates all required fields before saving and provides user feedback.
     * On successful save, triggers the layout content replacement to show ride summary.
     *
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Current location must not be empty</li>
     *   <li>Destination must not be empty</li>
     *   <li>Number of seats must not be empty</li>
     *   <li>Date and time must be selected</li>
     *   <li>User must be authenticated</li>
     * </ul>
     */
    private void saveDriversData() {
        String currentLoc = editTextCurrentLocation.getText().toString().trim();
        String destination = editTextDestination.getText().toString().trim();
        String numberOfSeats = editTextsSeats.getText().toString().trim();
        String comments = editTextComments.getText().toString().trim();
        String date = tvSelectedDate.getText().toString().replace("Date: ", "").trim();
        String time = tvSelectedTime.getText().toString().replace("Time: ", "").trim();

        if (currentLoc.isEmpty() || destination.isEmpty() || numberOfSeats.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userUID = currentUser.getUid();
        // Create a Ride object with the data
        Driver drive = new Driver(currentLoc, destination, numberOfSeats, comments, date, time, userUID);

        // Save the data to Firebase under Rides/userUID/rideId
        databaseReference.child(userUID).setValue(drive).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "Ride saved successfully!", Toast.LENGTH_SHORT).show();
                replaceLayoutContent();

            } else {
                Toast.makeText(requireContext(), "Failed to save ride details: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        /*BottomNavigationView nav = getActivity().findViewById(R.id.bottom_Navigation);
        nav.setSelectedItemId(R.id.passenger);*/
    }

    /**
     * Gets the current device location and displays the corresponding address.
     * Uses FusedLocationProviderClient to obtain location and Geocoder for reverse geocoding.
     * Requires fine location permission and handles permission requests if needed.
     *
     * <p>The method:</p>
     * <ul>
     *   <li>Checks for location permissions</li>
     *   <li>Requests location if permission is granted</li>
     *   <li>Performs reverse geocoding to get readable address</li>
     *   <li>Updates the current location EditText with the address</li>
     * </ul>
     */
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
                        editTextCurrentLocation.setText(currentAddress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error getting address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets up autocomplete functionality for the destination input field.
     * Integrates with Google Places API to provide location suggestions as the user types.
     * Creates an ArrayAdapter for displaying suggestions and sets up TextWatcher for input monitoring.
     */
    private void setupDestinationAutocomplete() {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        List<String> suggestions = new ArrayList<>();
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                suggestions
        );
        editTextDestination.setAdapter(destinationAdapter);

        editTextDestination.addTextChangedListener(new TextWatcher() {
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

    /**
     * Fetches autocomplete suggestions from Google Places API based on user input.
     * Filters results to Israeli locations and updates the adapter with new suggestions.
     *
     * @param query the search query entered by the user
     * @param token the autocomplete session token for API requests
     * @param destinationAdapter the ArrayAdapter to update with new suggestions
     */
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

    /**
     * Callback method triggered when Google Maps is ready for use.
     * Enables user location display if permission is granted and updates map with current location.
     *
     * @param googleMap the GoogleMap instance that is ready for use
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Enable user location if permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }

        // Update the map location if we already have it
        if (currentLocation != null) {
            updateMapLocation();
        }
    }

    /**
     * Retrieves the device's last known location using FusedLocationProviderClient.
     * Handles permission checking and updates the map location when successful.
     * This method is called when current location is not available.
     */
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
                if (gMap != null) {
                    updateMapLocation();
                }
            }
        });
    }

    /**
     * Updates the Google Maps display with the current location.
     * Centers the map camera on the current location and adds a marker.
     * This method is called after location is obtained and map is ready.
     */
    private void updateMapLocation() {
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        gMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
    }

    /**
     * Saves the current location to the instance state for configuration changes.
     * Ensures location data persists through device rotations and other configuration changes.
     *
     * @param outState the Bundle in which to place the saved state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current location to the instance state
        outState.putParcelable("currentLocation", currentLocation);
    }

    /**
     * Handles the result of location permission requests.
     * If permission is granted, attempts to get the last location.
     * If denied, shows a toast message informing the user.
     *
     * @param requestCode the request code passed to requestPermissions()
     * @param permissions the requested permissions
     * @param grantResults the grant results for the corresponding permissions
     */
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