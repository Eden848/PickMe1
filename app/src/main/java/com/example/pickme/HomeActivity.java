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


public class HomeActivity extends AppCompatActivity {


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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id.fragmentContainer, fragment)
                .commit();

    }


}
