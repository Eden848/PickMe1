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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail;
    private EditText etName, etAge;
    private Button btnUpdate, btnLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        tvEmail = findViewById(R.id.tvEmail);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnLogout = findViewById(R.id.btnLogout);

        tvEmail.setText(currentUser.getEmail());


        etName.setEnabled(false);
        etAge.setEnabled(false);

        loadUserData();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    // Enable editing
                    etName.setEnabled(true);
                    etAge.setEnabled(true);
                    btnUpdate.setText("Save Changes");
                    isEditing = true;
                } else {
                    // Save changes
                    updateUserData();
                    etName.setEnabled(false);
                    etAge.setEnabled(false);
                    btnUpdate.setText("Update Profile");
                    isEditing = false;
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void loadUserData() {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);

                    etName.setText(name != null ? name : "");
                    etAge.setText(age != null ? age : "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        // Validate name: Only letters allowed
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

        mDatabaseRef.child("name").setValue(name);
        mDatabaseRef.child("age").setValue(age).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("StayConnected", false);
                    editor.apply();

                    mAuth.signOut();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    Toast.makeText(ProfileActivity.this, "Log out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }


}
