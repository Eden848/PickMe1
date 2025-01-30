package com.example.pickme;

import static com.example.pickme.R.*;



import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(id.fragmentContainer, fragment)
                .commit();

    }

}
