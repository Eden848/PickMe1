package com.example.pickme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * {@code ProfileActivity} is an Android Activity that allows users to view and update their profile
 * information (name and age) stored in Firebase Realtime Database. It also provides functionality
 * for users to log out of their account.
 * The user's email is displayed, and profile fields can be enabled for editing.
 */
public class ProfileActivity extends AppCompatActivity {

    /**
     * TextView to display the current user's email address.
     */
    private TextView tvEmail;
    /**
     * EditText for displaying and editing the user's name.
     */
    private EditText etName;
    /**
     * EditText for displaying and editing the user's age.
     */
    private EditText etAge;
    /**
     * Button to toggle between edit mode and save changes, or to initiate profile update.
     */
    private Button btnUpdate;
    /**
     * Button to log out the current user.
     */
    private Button btnLogout;

    /**
     * FirebaseAuth instance for managing user authentication state.
     */
    private FirebaseAuth mAuth;
    /**
     * DatabaseReference to the current user's data node in Firebase Realtime Database.
     */
    private DatabaseReference mDatabaseRef;
    /**
     * The currently authenticated Firebase user.
     */
    private FirebaseUser currentUser;

    /**
     * A boolean flag indicating whether the profile fields are currently in editing mode.
     * True if editable, false otherwise.
     */
    private boolean isEditing = false;

    /**
     * Called when the activity is first created. This method initializes Firebase instances,
     * links UI elements to their respective views in the layout, sets up initial UI state,
     * loads existing user data, and attaches click listeners to buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // Initialize database reference to the current user's specific node
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Initialize UI components by finding their IDs in the layout
        tvEmail = findViewById(R.id.tvEmail);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnLogout = findViewById(R.id.btnLogout);

        // Set the email TextView with the current user's email
        tvEmail.setText(currentUser.getEmail());

        // Disable name and age EditText fields initially
        etName.setEnabled(false);
        etAge.setEnabled(false);

        // Load existing user data from Firebase
        loadUserData();

        // Set OnClickListener for the update button
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    // If not in editing mode, enable fields and change button text to "Save Changes"
                    etName.setEnabled(true);
                    etAge.setEnabled(true);
                    btnUpdate.setText("Save Changes");
                    isEditing = true; // Set editing flag to true
                } else {
                    // If in editing mode, save changes, disable fields, and change button text back
                    updateUserData(); // Call method to save data to Firebase
                    etName.setEnabled(false);
                    etAge.setEnabled(false);
                    btnUpdate.setText("Update Profile");
                    isEditing = false; // Set editing flag to false
                }
            }
        });

        // Set OnClickListener for the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser(); // Call method to log out the user
            }
        });
    }

    /**
     * Loads the current user's profile data (name and age) from the Firebase Realtime Database
     * and populates the corresponding EditText fields.
     * A Toast message is displayed if data loading fails.
     */
    private void loadUserData() {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve name and age from the snapshot
                    String name = snapshot.child("name").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);

                    // Set the text of EditText fields, handling null values
                    etName.setText(name != null ? name : "");
                    etAge.setText(age != null ? age : "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Display a toast message if data loading is cancelled or fails
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the current user's profile data (name and age) in the Firebase Realtime Database.
     * It performs input validation for name (letters only) and age (numbers only).
     * A Toast message indicates whether the update was successful or failed.
     */
    private void updateUserData() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        // Validate name: Only letters and spaces allowed
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            etName.setError("Name must contain only letters");
            return;
        }

        // Validate age: Only numbers allowed
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            return;
        }
        if (!age.matches("\\d+")) {
            etAge.setError("Age must be a valid number");
            return;
        }

        // Update name and age in Firebase
        mDatabaseRef.child("name").setValue(name);
        mDatabaseRef.child("age").setValue(age).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Prompts the user with an AlertDialog to confirm logout.
     * If confirmed, it clears the "StayConnected" preference, signs out the user from Firebase,
     * navigates to the {@link MainActivity}, and finishes the current activity.
     */
    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Logout") // Set the title of the dialog
                .setMessage("Are you sure you want to log out?") // Set the message
                .setPositiveButton("Yes", (dialog, which) -> {
                    // On "Yes" click:
                    // Get SharedPreferences and clear "StayConnected" preference
                    SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("StayConnected", false);
                    editor.apply(); // Apply changes to SharedPreferences

                    mAuth.signOut(); // Sign out the user from Firebase
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class); // Create intent for MainActivity
                    Toast.makeText(ProfileActivity.this, "Log out successfully", Toast.LENGTH_SHORT).show(); // Show logout success message
                    startActivity(intent); // Start MainActivity
                    finish(); // Finish current activity
                })
                .setNegativeButton("No", null) // On "No" click, do nothing (dialog dismisses)
                .show(); // Show the AlertDialog
    }
}
