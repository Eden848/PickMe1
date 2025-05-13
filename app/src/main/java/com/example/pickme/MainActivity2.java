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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;


public class MainActivity2 extends AppCompatActivity {

    private Button sign_button;
    private Button tologin_button;
    private EditText name_input;
    private EditText age_input;
    private EditText username_sign_input;
    private EditText  password_sign_intup;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        sign_button = findViewById(R.id.sign_button);
        tologin_button = findViewById(R.id.tologin_button);
        name_input = findViewById(R.id.name_input);
        age_input = findViewById(R.id.age_input);
        username_sign_input = findViewById(R.id.username_sign_input);
        password_sign_intup = findViewById(R.id.password_sign_input);

        tologin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        sign_button.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = name_input.getText().toString().trim();
        String age = age_input.getText().toString().trim();
        String email = username_sign_input.getText().toString().trim();
        String password = password_sign_intup.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            User user1 = new User(name, age, email);

                            databaseReference.child(userId).setValue(user1)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {

                                            firebaseUser.sendEmailVerification()
                                                    .addOnCompleteListener(emailTask -> {
                                                        if (emailTask.isSuccessful()) {
                                                            Toast.makeText(MainActivity2.this, "Sign up successful! Please check your email to verify your account.", Toast.LENGTH_LONG).show();
                                                            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(MainActivity2.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(MainActivity2.this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity2.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


