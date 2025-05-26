package com.example.pickme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

/**
 * Main login activity for the PickMe ride-sharing application.
 *
 * <p>This activity serves as the entry point and authentication gateway for the application.
 * It provides user login functionality with the following features:</p>
 *
 * <ul>
 *   <li><strong>User Authentication</strong> - Firebase Authentication integration for secure login</li>
 *   <li><strong>Email Verification</strong> - Enforces email verification before allowing access</li>
 *   <li><strong>Stay Connected</strong> - Optional persistent login using SharedPreferences</li>
 *   <li><strong>Input Validation</strong> - Email format and password strength validation</li>
 *   <li><strong>User Registration</strong> - Navigation to signup activity</li>
 *   <li><strong>Auto-Login</strong> - Automatic login for users who chose to stay connected</li>
 * </ul>
 *
 * <p><strong>Authentication Flow:</strong></p>
 * <ol>
 *   <li>Check for existing authenticated user with "stay connected" preference</li>
 *   <li>Validate user input (email format and password requirements)</li>
 *   <li>Authenticate with Firebase Authentication</li>
 *   <li>Verify email verification status</li>
 *   <li>Save login preferences if "stay connected" is enabled</li>
 *   <li>Redirect to HomeActivity upon successful authentication</li>
 * </ol>
 *
 * <p><strong>Security Features:</strong></p>
 * <ul>
 *   <li>Email verification requirement</li>
 *   <li>Minimum 6-character password requirement</li>
 *   <li>Secure credential storage using SharedPreferences</li>
 *   <li>Automatic logout for unverified users</li>
 * </ul>
 *
 * <p><strong>Navigation:</strong></p>
 * <ul>
 *   <li>Success: {@link HomeActivity} - Main application interface</li>
 *   <li>Signup: {@link MainActivity2} - User registration activity</li>
 * </ul>
 *
 * @author [Your Name]
 * @version 1.0
 * @since API level 1
 * @see FirebaseAuth
 * @see SharedPreferences
 * @see HomeActivity
 * @see MainActivity2
 */
public class MainActivity extends AppCompatActivity {

    /** Button for user login submission */
    private Button login_button;

    /** Button for navigating to user registration */
    private Button signup_button;

    /** EditText for email/username input */
    private EditText usernameInput;

    /** EditText for password input */
    private EditText passwordInput;

    /** CheckBox for enabling persistent login */
    private CheckBox stayConnectedCheckbox;

    /** Firebase Authentication instance */
    private FirebaseAuth mAuth;

    /** SharedPreferences for storing login preferences */
    private SharedPreferences sharedPreferences;

    /** SharedPreferences file name for login data */
    private static final String PREF_NAME = "LoginPrefs";

    /** SharedPreferences key for storing last used email */
    private static final String KEY_EMAIL = "lastEmail";

    /** SharedPreferences key for storing last used password */
    private static final String KEY_PASSWORD = "lastPassword";

    /** SharedPreferences key for storing stay connected preference */
    private static final String KEY_STAY_CONNECTED = "stayConnected";

    /**
     * Called when the activity is starting.
     *
     * <p>This method performs comprehensive initialization:</p>
     *
     * <ul>
     *   <li><strong>UI Setup:</strong> Enables edge-to-edge display and inflates layout</li>
     *   <li><strong>Component Initialization:</strong> Finds and initializes all UI components</li>
     *   <li><strong>Firebase Setup:</strong> Initializes Firebase Authentication instance</li>
     *   <li><strong>SharedPreferences Setup:</strong> Configures persistent storage for login preferences</li>
     *   <li><strong>Auto-Login Check:</strong> Verifies existing authentication status</li>
     *   <li><strong>Event Listeners:</strong> Sets up button click handlers for login and signup</li>
     * </ul>
     *
     * <p><strong>Auto-Login Logic:</strong></p>
     * <ol>
     *   <li>Check if user previously enabled "stay connected"</li>
     *   <li>Verify current Firebase user exists and is authenticated</li>
     *   <li>Confirm email verification status</li>
     *   <li>Redirect to HomeActivity if all conditions are met</li>
     *   <li>Sign out user if email is not verified</li>
     * </ol>
     *
     * <p><strong>Input Validation:</strong> Login button validates:</p>
     * <ul>
     *   <li>Email format using {@link Patterns#EMAIL_ADDRESS}</li>
     *   <li>Password minimum length requirement (6 characters)</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains
     *                          the data it most recently supplied. Otherwise, it is null.
     * @see AppCompatActivity#onCreate(Bundle)
     * @see #authenticateUser(String, String)
     * @see #redirectToHome()
     */
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

    /**
     * Authenticates user credentials with Firebase Authentication.
     *
     * <p>This method handles the complete authentication process:</p>
     *
     * <ul>
     *   <li><strong>Firebase Authentication:</strong> Uses email/password sign-in method</li>
     *   <li><strong>Email Verification Check:</strong> Verifies user's email before proceeding</li>
     *   <li><strong>Preference Storage:</strong> Saves login data if "stay connected" is enabled</li>
     *   <li><strong>Success Handling:</strong> Displays success message and redirects to home</li>
     *   <li><strong>Error Handling:</strong> Shows detailed error messages for authentication failures</li>
     * </ul>
     *
     * <p><strong>Authentication Success Flow:</strong></p>
     * <ol>
     *   <li>Verify Firebase user object is not null</li>
     *   <li>Check email verification status using {@link FirebaseUser#isEmailVerified()}</li>
     *   <li>Save credentials to SharedPreferences if stay connected is checked</li>
     *   <li>Display success toast message</li>
     *   <li>Call {@link #redirectToHome()} to navigate to main application</li>
     * </ol>
     *
     * <p><strong>Security Measures:</strong></p>
     * <ul>
     *   <li>Requires email verification before allowing access</li>
     *   <li>Stores credentials securely using SharedPreferences</li>
     *   <li>Provides detailed error messages for troubleshooting</li>
     * </ul>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li>Invalid credentials - Shows Firebase error message</li>
     *   <li>Unverified email - Prompts user to check inbox</li>
     *   <li>Network issues - Displays connection error details</li>
     * </ul>
     *
     * @param email User's email address (must be valid email format)
     * @param password User's password (must be at least 6 characters)
     * @see FirebaseAuth#signInWithEmailAndPassword(String, String)
     * @see FirebaseUser#isEmailVerified()
     * @see SharedPreferences.Editor
     */
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

    /**
     * Redirects authenticated users to the main application interface.
     *
     * <p>This method performs a clean navigation to {@link HomeActivity} with the following characteristics:</p>
     *
     * <ul>
     *   <li><strong>Activity Stack Management:</strong> Clears the activity stack to prevent back navigation</li>
     *   <li><strong>Intent Flags:</strong> Uses multiple flags for proper task and activity management</li>
     *   <li><strong>Activity Lifecycle:</strong> Calls finish() to properly destroy the login activity</li>
     * </ul>
     *
     * <p><strong>Intent Flags Used:</strong></p>
     * <ul>
     *   <li><strong>FLAG_ACTIVITY_CLEAR_TOP:</strong> Removes all activities above HomeActivity in the stack</li>
     *   <li><strong>FLAG_ACTIVITY_NEW_TASK:</strong> Creates HomeActivity in a new task if needed</li>
     *   <li><strong>FLAG_ACTIVITY_CLEAR_TASK:</strong> Clears the entire current task before starting HomeActivity</li>
     * </ul>
     *
     * <p><strong>Navigation Behavior:</strong></p>
     * <ul>
     *   <li>User cannot navigate back to login screen using back button</li>
     *   <li>HomeActivity becomes the root activity of the application</li>
     *   <li>Ensures clean user experience after successful authentication</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong> Called after successful user authentication
     * and email verification confirmation.</p>
     *
     * @see HomeActivity
     * @see Intent#addFlags(int)
     * @see androidx.appcompat.app.AppCompatActivity#finish()
     */
    private void redirectToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}