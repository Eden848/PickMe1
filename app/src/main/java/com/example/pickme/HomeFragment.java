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

/**
 * Main home fragment for the PickMe ride-sharing application.
 *
 * <p>This fragment serves as the primary interface for passengers to:</p>
 * <ul>
 *   <li>View their current location on an interactive Google Map</li>
 *   <li>Enter trip details including pickup location, destination, and preferences</li>
 *   <li>Search for destinations using Google Places autocomplete</li>
 *   <li>Submit ride requests and proceed to driver selection</li>
 *   <li>Access user profile information</li>
 * </ul>
 *
 * <p>The fragment integrates multiple Google Play Services:</p>
 * <ul>
 *   <li><strong>Google Maps</strong> - Interactive map display with real-time location tracking</li>
 *   <li><strong>Fused Location Provider</strong> - Continuous location updates and geocoding</li>
 *   <li><strong>Places API</strong> - Destination autocomplete suggestions (Israel-focused)</li>
 * </ul>
 *
 * <p>Firebase integration provides:</p>
 * <ul>
 *   <li><strong>Authentication</strong> - User session management and validation</li>
 *   <li><strong>Realtime Database</strong> - User profile data and trip information storage</li>
 * </ul>
 *
 * <p><strong>Location Permissions:</strong> This fragment requires ACCESS_FINE_LOCATION
 * permission for map functionality and real-time location tracking.</p>
 *
 * <p><strong>Language Support:</strong> Configured for Hebrew locale ("he") to support
 * Israeli users and locations.</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since API level 1
 * @see OnMapReadyCallback
 * @see Fragment
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Request code for fine location permission requests
     */
    private static final int FINE_LOCATION_REQUEST_CODE = 1;

    /**
     * Google Map instance for displaying interactive map
     */
    private GoogleMap mMap;

    /**
     * Current user location from location services
     */
    private Location currentLocation;

    /**
     * Client for accessing fused location provider services
     */
    private FusedLocationProviderClient fusedLocationProviderClient;

    /**
     * Callback for handling location update results
     */
    private LocationCallback locationCallback;

    /**
     * Flag to track if this is the first location update (for initial map setup)
     */
    private boolean isFirstLocationUpdate = true;

    /**
     * Google Places API client for autocomplete functionality
     */
    private PlacesClient placesClient;

    /**
     * Firebase authentication instance
     */
    private FirebaseAuth firebaseAuth;

    /**
     * Firebase database reference for user data
     */
    private DatabaseReference databaseReference;

    /**
     * TextView displaying personalized welcome message with user name
     */
    private TextView userNameTextView;

    /**
     * EditText for current location/pickup address
     */
    private EditText loc;

    /**
     * EditText for number of seats required
     */
    private EditText set;

    /**
     * EditText for additional comments
     */
    private EditText com;

    /**
     * AutoCompleteTextView for destination input with place suggestions
     */
    private AutoCompleteTextView des;

    /**
     * Button to submit trip request
     */
    private Button btnSub;

    /**
     * Button to navigate to user profile
     */
    private ImageButton profileButton;

    /**
     * ValueEventListener for real-time user name updates from Firebase
     */
    private ValueEventListener userNameListener;

    /**
     * Called when the fragment is first created.
     *
     * <p>This method initializes core services and components:</p>
     * <ul>
     *   <li>Firebase Authentication and Database instances</li>
     *   <li>Fused Location Provider Client for location services</li>
     *   <li>Google Places API client with Hebrew locale support</li>
     *   <li>Location callback for continuous location updates</li>
     * </ul>
     *
     * <p>The location callback handles real-time location updates, map camera movement,
     * and address geocoding for the pickup location field.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from a previous
     *                           saved state, this is the state. Otherwise null.
     * @see Fragment#onCreate(Bundle)
     * @see LocationCallback
     * @see Places#initialize(android.content.Context, String, java.util.Locale)
     */
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

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Inflates the fragment layout from {@code R.layout.fragment_home}</li>
     *   <li>Initializes all UI components and their references</li>
     *   <li>Sets up event listeners for buttons and user interactions</li>
     *   <li>Configures the destination autocomplete functionality</li>
     *   <li>Initializes the Google Maps fragment</li>
     *   <li>Fetches and displays the current user's name</li>
     * </ul>
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          If non-null, this is the parent view that the fragment's UI will be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @see #setupDestinationAutocomplete()
     * @see #fetchUserName()
     */
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

    /**
     * Called when the fragment becomes visible to the user.
     *
     * <p>Starts location updates to ensure real-time location tracking
     * is active while the user is viewing the fragment.</p>
     *
     * @see Fragment#onResume()
     * @see #startLocationUpdates()
     */
    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    /**
     * Called when the fragment is no longer visible to the user.
     *
     * <p>Stops location updates to conserve battery and system resources
     * when the fragment is not actively being viewed.</p>
     *
     * @see Fragment#onPause()
     * @see #stopLocationUpdates()
     */
    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Initiates continuous location updates from the fused location provider.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Checks for ACCESS_FINE_LOCATION permission</li>
     *   <li>Requests permission if not already granted</li>
     *   <li>Configures location request with high accuracy and 5-second intervals</li>
     *   <li>Starts location updates with the configured callback</li>
     * </ul>
     *
     * <p><strong>Location Request Configuration:</strong></p>
     * <ul>
     *   <li>Update Interval: 5000ms (5 seconds)</li>
     *   <li>Priority: High Accuracy</li>
     *   <li>Minimum Update Interval: 2000ms (2 seconds)</li>
     * </ul>
     *
     * @see FusedLocationProviderClient#requestLocationUpdates(LocationRequest, LocationCallback, Looper)
     * @see #FINE_LOCATION_REQUEST_CODE
     */
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

    /**
     * Stops all active location updates to conserve system resources.
     *
     * <p>This method should be called when location tracking is no longer needed,
     * such as when the fragment is paused or destroyed.</p>
     *
     * @see FusedLocationProviderClient#removeLocationUpdates(LocationCallback)
     */
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Fetches and displays the current user's name from Firebase Realtime Database.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Verifies user authentication status</li>
     *   <li>Sets up a real-time listener for the user's name field</li>
     *   <li>Updates the welcome message TextView with personalized greeting</li>
     *   <li>Handles authentication failures by redirecting to login</li>
     *   <li>Manages listener lifecycle to prevent memory leaks</li>
     * </ul>
     *
     * <p><strong>Database Structure:</strong> {@code Users/{userId}/name}</p>
     *
     * <p><strong>Fallback Behavior:</strong> Displays "Welcome, User!" if name is unavailable</p>
     *
     * @see FirebaseAuth#getCurrentUser()
     * @see DatabaseReference#addValueEventListener(ValueEventListener)
     * @see #redirectToLogin()
     */
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

    /**
     * Validates and saves passenger trip data to Firebase Realtime Database.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Validates all required form fields (location, destination, seats)</li>
     *   <li>Verifies user authentication status</li>
     *   <li>Creates a Passenger object with the collected data</li>
     *   <li>Saves data to Firebase under the user's profile</li>
     *   <li>Navigates to driver selection activity upon success</li>
     *   <li>Displays appropriate error messages for failures</li>
     * </ul>
     *
     * <p><strong>Required Fields:</strong></p>
     * <ul>
     *   <li>Current Location (pickup address)</li>
     *   <li>Destination address</li>
     *   <li>Number of seats required</li>
     * </ul>
     *
     * <p><strong>Optional Fields:</strong></p>
     * <ul>
     *   <li>Additional comments/preferences</li>
     * </ul>
     *
     * <p><strong>Database Structure:</strong> {@code Users/{userId}/Passenger}</p>
     *
     * @see Passenger
     * @see PickDriverActivity
     * @see DatabaseReference#setValue(Object)
     */
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

    /**
     * Redirects unauthenticated users to the login activity.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Creates an intent to navigate to MainActivity (login screen)</li>
     *   <li>Clears the activity stack to prevent back navigation</li>
     *   <li>Finishes the current activity to ensure clean logout</li>
     * </ul>
     *
     * <p><strong>Intent Flags:</strong></p>
     * <ul>
     *   <li>FLAG_ACTIVITY_CLEAR_TOP - Removes all activities above MainActivity</li>
     *   <li>FLAG_ACTIVITY_NEW_TASK - Creates MainActivity in a new task</li>
     *   <li>FLAG_ACTIVITY_CLEAR_TASK - Clears the entire task stack</li>
     * </ul>
     *
     * @see MainActivity
     * @see Intent#addFlags(int)
     */
    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity to prevent back navigation
    }

    /**
     * Performs reverse geocoding to convert location coordinates to a readable address.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Uses Android's Geocoder with Hebrew locale support</li>
     *   <li>Converts latitude/longitude to a formatted address string</li>
     *   <li>Updates the location EditText with the resolved address</li>
     *   <li>Handles geocoding errors gracefully with user feedback</li>
     * </ul>
     *
     * <p><strong>Locale:</strong> Configured for Hebrew ("he") to provide
     * localized address formatting for Israeli locations.</p>
     *
     * <p><strong>Error Handling:</strong> Displays toast message if geocoding fails
     * due to network issues or service limitations.</p>
     *
     * @param location The Location object containing latitude and longitude coordinates
     * @throws IOException if the geocoding service is unavailable or encounters an error
     * @see Geocoder#getFromLocation(double, double, int)
     * @see Address#getAddressLine(int)
     */
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

    /**
     * Sets up autocomplete functionality for the destination input field.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Creates an autocomplete session token for Places API requests</li>
     *   <li>Initializes an ArrayAdapter for displaying suggestions</li>
     *   <li>Sets up a TextWatcher to trigger autocomplete on text changes</li>
     *   <li>Configures the AutoCompleteTextView with the adapter</li>
     * </ul>
     *
     * <p><strong>Autocomplete Features:</strong></p>
     * <ul>
     *   <li>Real-time place suggestions as user types</li>
     *   <li>Custom dropdown item layout (R.layout.dropdown_item)</li>
     *   <li>Automatic adapter updates with new suggestions</li>
     * </ul>
     *
     * @see AutocompleteSessionToken
     * @see TextWatcher
     * @see #fetchAutocompleteSuggestions(String, AutocompleteSessionToken, ArrayAdapter)
     */
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    fetchAutocompleteSuggestions(s.toString(), token, destinationAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Fetches place autocomplete suggestions from Google Places API.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Creates a FindAutocompletePredictionsRequest with the user's query</li>
     *   <li>Restricts results to Israel ("IL") for relevant local suggestions</li>
     *   <li>Processes API response and extracts place names</li>
     *   <li>Updates the destination adapter with new suggestions</li>
     *   <li>Handles API errors with user-friendly error messages</li>
     * </ul>
     *
     * <p><strong>Request Configuration:</strong></p>
     * <ul>
     *   <li>Query: User's input text</li>
     *   <li>Session Token: For billing and caching optimization</li>
     *   <li>Country Filter: Israel ("IL") only</li>
     * </ul>
     *
     * <p><strong>Response Processing:</strong> Extracts full text descriptions
     * from AutocompletePrediction objects for display in dropdown.</p>
     *
     * @param query              The search query entered by the user
     * @param token              Session token for API request optimization and billing
     * @param destinationAdapter ArrayAdapter to update with new suggestions
     * @see FindAutocompletePredictionsRequest
     * @see AutocompletePrediction#getFullText(android.text.style.CharacterStyle)
     * @see PlacesClient#findAutocompletePredictions(FindAutocompletePredictionsRequest)
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
     * Callback method invoked when the Google Map is ready for use.
     *
     * <p>This method configures the map with:</p>
     * <ul>
     *   <li>Custom map styling (if available from R.raw.map_style)</li>
     *   <li>Location permission verification and request</li>
     *   <li>My Location layer and button enablement</li>
     *   <li>Zoom controls and UI settings configuration</li>
     *   <li>Map type set to normal for standard tile display</li>
     *   <li>Automatic location updates startup</li>
     * </ul>
     *
     * <p><strong>Map Configuration:</strong></p>
     * <ul>
     *   <li>Type: Normal (standard Google Maps tiles)</li>
     *   <li>My Location: Enabled (with permission)</li>
     *   <li>Zoom Controls: Enabled</li>
     *   <li>Custom Styling: Applied if map_style.json exists</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong> Gracefully handles missing map styles
     * and permission denial scenarios.</p>
     *
     * @param googleMap The GoogleMap instance that is ready for use
     * @see OnMapReadyCallback#onMapReady(GoogleMap)
     * @see GoogleMap#setMyLocationEnabled(boolean)
     * @see #startLocationUpdates()
     */
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
}

/**
 * Handles the result of runtime permission requests.
 *
 * <p>This method specifically handles ACCESS_FINE_LOCATION permission results:</p>
 * <ul>
 *   <li><strong>Permission Granted:</strong> Starts location updates and enables map location features</li>
 *   <li><strong>Permission Denied:</strong> Displays informative message about functionality limitations</li>
 * </ul>
 *
 * <p><strong>Actions on Permission Grant:</strong></p>
 * <ul>
 *   <li>Initiates location updates via {@link #startLocationUpdates()}</li>
 *   <li>Enables My Location layer on the map (if map is ready)</li>
 *   <li>Shows My Location button in map UI</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong> Catches SecurityException when enabling
 * map location features and logs the error.</p>
 *
 * @param requestCode The request code passed to requestPermissions()
 * @param permissions The requested permissions (never null)
 * @param grantResults The grant results for the corresponding permissions*/