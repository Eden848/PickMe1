package com.example.pickme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button login_button;
    private Button signup_button;
    private EditText usernameInput;
    private EditText passwordInput;
    private CheckBox stayConnectedCheckbox;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "lastEmail";
    private static final String KEY_PASSWORD = "lastPassword";
    private static final String KEY_STAY_CONNECTED = "stayConnected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        signup_button = findViewById(R.id.signup_button);
        login_button = findViewById(R.id.login_button);
        usernameInput = findViewById(R.id.username_sign_input);
        passwordInput = findViewById(R.id.password_sign_input);
        stayConnectedCheckbox = findViewById(R.id.stay_connected_checkbox);
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (!sharedPreferences.contains(KEY_STAY_CONNECTED)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_STAY_CONNECTED, false);
            editor.apply();
        }

        stayConnectedCheckbox.setChecked(false);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean stayConnected = sharedPreferences.getBoolean(KEY_STAY_CONNECTED, false);

        if (stayConnected && currentUser != null) {
            if (currentUser.isEmailVerified()) {
                redirectToHome();
            } else {
                mAuth.signOut();
            }
        }

        login_button.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                usernameInput.setError("Invalid email format");
                usernameInput.requestFocus();
                return;
            }

            if (password.isEmpty() || password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                passwordInput.requestFocus();
                return;
            }

            authenticateUser(email, password);
        });

        // Set up signup button functionality
        signup_button.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });
    }

    private void authenticateUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            if (user.isEmailVerified()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_EMAIL, email);
                                editor.putString(KEY_PASSWORD, password);
                                editor.putBoolean(KEY_STAY_CONNECTED, stayConnectedCheckbox.isChecked());
                                editor.apply();

                                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                redirectToHome();
                            } else {
                                Toast.makeText(MainActivity.this, "Email not verified. Please check your inbox.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void redirectToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
