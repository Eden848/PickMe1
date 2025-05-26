package com.example.pickme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity2 handles user registration functionality for the PickMe application.
 * This activity provides a comprehensive sign-up form where new users can create accounts
 * using Firebase Authentication and have their profile data stored in Firebase Realtime Database.
 *
 * <p>The registration process includes:</p>
 * <ul>
 *   <li>Input validation for all required fields</li>
 *   <li>Firebase Authentication account creation</li>
 *   <li>User profile storage in Realtime Database</li>
 *   <li>Email verification sending</li>
 *   <li>Navigation flow management</li>
 * </ul>
 *
 * <p>This activity works in conjunction with Firebase services to provide
 * a complete user onboarding experience with proper error handling and
 * user feedback through Toast messages.</p>
 *
 * @author Your Name
 * @version 1.0
 * @since API Level 21
 */
public class MainActivity2 extends AppCompatActivity {

    /** Button to initiate the user sign-up process */
    private Button sign_button;

    /** Button to navigate back to the login screen (MainActivity) */
    private Button tologin_button;

    /** Input field for capturing the user's full name */
    private EditText name_input;

    /** Input field for capturing the user's age */
    private EditText age_input;

    /** Input field for capturing the user's email address (used as username) */
    private EditText username_sign_input;

    /** Input field for capturing the user's chosen password */
    private EditText password_sign_intup;

    /** Firebase Authentication instance for managing user authentication */
    private FirebaseAuth mAuth;

    /** Database reference pointing to the "Users" node in Firebase Realtime Database */
    private DatabaseReference databaseReference;

    /**
     * Called when the activity is starting. This method initializes the user interface,
     * sets up Firebase services, connects UI components, and configures event listeners
     * for user interactions.
     *
     * <p>The method performs the following operations:</p>
     * <ul>
     *   <li>Enables edge-to-edge display</li>
     *   <li>Sets the content view layout</li>
     *   <li>Initializes Firebase Authentication and Database instances</li>
     *   <li>Binds UI components to their corresponding layout elements</li>
     *   <li>Sets up click listeners for interactive buttons</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains the data
     *                          it most recently supplied in onSaveInstanceState(Bundle).
     *                          Note: Otherwise it is null.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Bind UI components to layout elements
        sign_button = findViewById(R.id.sign_button);
        tologin_button = findViewById(R.id.tologin_button);
        name_input = findViewById(R.id.name_input);
        age_input = findViewById(R.id.age_input);
        username_sign_input = findViewById(R.id.username_sign_input);
        password_sign_intup = findViewById(R.id.password_sign_input);

        // Set up navigation to login screen
        tologin_button.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles click events on the "To Login" button.
             * Creates an intent to navigate back to the login screen (MainActivity)
             * when the user wants to return to the login page instead of signing up.
             *
             * @param view The view that was clicked (tologin_button)
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up sign-up process trigger
        sign_button.setOnClickListener(v -> signUpUser());
    }

    /**
     * Handles the complete user registration process including input validation,
     * Firebase Authentication account creation, database storage, and email verification.
     *
     * <p>The sign-up process follows these steps:</p>
     * <ol>
     *   <li>Extract and trim user input from all form fields</li>
     *   <li>Validate that all required fields are filled</li>
     *   <li>Validate password meets minimum length requirement (6 characters)</li>
     *   <li>Create Firebase Authentication account with email and password</li>
     *   <li>Store user profile data in Firebase Realtime Database</li>
     *   <li>Send email verification to the user</li>
     *   <li>Navigate to login screen upon successful completion</li>
     * </ol>
     *
     * <p>Error handling is implemented at each step with appropriate user feedback
     * through Toast messages. The method uses Firebase's asynchronous task completion
     * listeners to handle the multi-step registration process.</p>
     *
     * <p>Validation Rules:</p>
     * <ul>
     *   <li>All fields (name, age, email, password) must be non-empty</li>
     *   <li>Password must be at least 6 characters long</li>
     *   <li>Email must be in valid format (validated by Firebase)</li>
     * </ul>
     *
     * @throws SecurityException if Firebase Authentication fails due to invalid credentials
     * @throws DatabaseException if Firebase Realtime Database operation fails
     *
     */
    private void signUpUser() {
        // Extract user input and remove leading/trailing whitespace
        String name = name_input.getText().toString().trim();
        String age = age_input.getText().toString().trim();
        String email = username_sign_input.getText().toString().trim();
        String password = password_sign_intup.getText().toString().trim();

        // Validate all required fields are filled
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password meets minimum length requirement
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Authentication successful - get the created user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            // Get unique user ID for database storage
                            String userId = firebaseUser.getUid();

                            // Create User object with profile information
                            User user1 = new User(name, age, email);

                            // Store user profile in Firebase Realtime Database
                            databaseReference.child(userId).setValue(user1)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            // Database storage successful - send email verification
                                            firebaseUser.sendEmailVerification()
                                                    .addOnCompleteListener(emailTask -> {
                                                        if (emailTask.isSuccessful()) {
                                                            // Complete success - inform user and navigate to login
                                                            Toast.makeText(MainActivity2.this, "Sign up successful! Please check your email to verify your account.", Toast.LENGTH_LONG).show();
                                                            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // Email verification failed
                                                            Toast.makeText(MainActivity2.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // Database storage failed
                                            Toast.makeText(MainActivity2.this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Authentication failed
                        Toast.makeText(MainActivity2.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}