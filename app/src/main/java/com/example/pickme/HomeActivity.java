package com.example.pickme;

import static com.example.pickme.R.*;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity class for the PickMe application home screen.
 *
 * <p>This activity serves as the primary navigation hub for the application,
 * managing fragment transitions through a bottom navigation bar and handling
 * the initialization of background services for ride monitoring.</p>
 *
 * <p>The activity provides four main sections accessible through bottom navigation:</p>
 * <ul>
 *   <li>Home - Main dashboard/overview screen</li>
 *   <li>Passenger - Passenger-specific functionality</li>
 *   <li>Driver - Driver-specific functionality</li>
 *   <li>History - Trip history and records</li>
 * </ul>
 *
 * <p>Additionally, this activity automatically starts the RideMonitorService
 * to track ride-related activities in the background.</p>
 *
 * @author [Your Name]
 * @version 1.0
 * @since API level 1
 */
public class HomeActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting.
     *
     * <p>This method performs the following initialization tasks:</p>
     * <ul>
     *   <li>Sets the content view to the home activity layout</li>
     *   <li>Starts the RideMonitorService for background ride tracking</li>
     *   <li>Loads the default HomeFragment if this is a fresh start</li>
     *   <li>Sets up the bottom navigation view with fragment switching logic</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied. Otherwise, it is null.
     * @see AppCompatActivity#onCreate(Bundle)
     * @see #startRideMonitorService()
     * @see #loadFragment(Fragment)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startRideMonitorService();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_Navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                loadFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.passenger) {
                loadFragment(new PassengerFragment());
            } else if (item.getItemId() == R.id.driver) {
                loadFragment(new DriverFragment());
            } else if (item.getItemId() == R.id.history) {
                loadFragment(new HistoryFragment());
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;

        });
    }

    /**
     * Starts the RideMonitorService to track ride-related activities in the background.
     *
     * <p>This method handles the proper service startup based on the Android version:</p>
     * <ul>
     *   <li>For Android Oreo (API 26) and above: Uses {@code startForegroundService()}</li>
     *   <li>For earlier versions: Uses {@code startService()}</li>
     * </ul>
     *
     * <p>The service is essential for maintaining ride state and notifications
     * even when the app is not in the foreground.</p>
     *
     * @see RideMonitorService
     * @see Build.VERSION_CODES#O
     */
    private void startRideMonitorService() {
        Intent serviceIntent = new Intent(this, RideMonitorService.class);

        // For Android Oreo (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            // For earlier versions
            startService(serviceIntent);
        }
    }

    /**
     * Loads and displays the specified fragment in the main container.
     *
     * <p>This method replaces the current fragment in the fragment container
     * with the provided fragment using a fragment transaction. The transaction
     * is committed immediately.</p>
     *
     * <p>The fragment will be placed in the container with ID {@code R.id.fragmentContainer}.
     * Any existing fragment in this container will be replaced.</p>
     *
     * @param fragment The fragment to be loaded and displayed. Must not be null.
     * @throws IllegalArgumentException if the fragment parameter is null
     * @see Fragment
     * @see androidx.fragment.app.FragmentTransaction#replace(int, Fragment)
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id.fragmentContainer, fragment)
                .commit();
    }
}