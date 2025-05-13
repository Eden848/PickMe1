package com.example.pickme;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RideMonitorService extends Service {
    private static final String TAG = "RideMonitorService";
    private static final String CHANNEL_ID = "ride_notifications";
    private static final int NOTIFICATION_ID = 1;
    private DatabaseReference ridesRef;
    private Map<String, ChildEventListener> listenersMap = new HashMap<>();
    private Set<String> monitoredRides = new HashSet<>();

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createServiceNotification("Monitoring Rides..."));
        startListeningForRides();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) {
                Log.e(TAG, "Failed to create notification channel: NotificationManager is null");
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ride Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new ride bookings");
            manager.createNotificationChannel(channel);
        }
    }

    private void startListeningForRides() {
        Log.i(TAG, "startListeningForRides/method started");
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Log.e(TAG, "No user logged in");
            stopSelf();
            return;
        }

        String uid = fbUser.getUid();

        // Listen to all rides
        ridesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot rideSnapshot, @Nullable String previousChildName) {
                String rideId = rideSnapshot.getKey();
                Log.i(TAG, "Found ride: " + rideId);

                // Check if this ride belongs to the current user by checking the uid field
                if (rideSnapshot.hasChild("uid")) {
                    String rideUid = rideSnapshot.child("uid").getValue(String.class);
                    if (uid.equals(rideUid)) {
                        Log.i(TAG, "This ride belongs to the current user: " + rideId);
                        monitoredRides.add(rideId);
                        attachPassengerListener(rideId);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String rideId = snapshot.getKey();
                if (monitoredRides.contains(rideId)) {
                    Log.i(TAG, "Removing listeners for ride: " + rideId);
                    if (listenersMap.containsKey(rideId)) {
                        ridesRef.child(rideId).child("Passengers").removeEventListener(listenersMap.get(rideId));
                        listenersMap.remove(rideId);
                    }
                    monitoredRides.remove(rideId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error in ride listener: " + error.getMessage());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        });
    }

    private void attachPassengerListener(String rideId) {
        Log.i(TAG, "Attaching passenger listener for ride: " + rideId);

        // The path should be: Rides -> RideID -> Passengers
        DatabaseReference passengersRef = ridesRef.child(rideId).child("Passengers");

        // First, check if there are already passengers
        passengersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Log.d(TAG, "Ride " + rideId + " already has passengers. Sending notification.");
                    sendDetailedNotification("Ride Booking", "You have passengers for your ride");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking existing passengers: " + error.getMessage());
            }
        });

        // Now listen for new passengers
        ChildEventListener passengerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i(TAG, "New passenger added for ride: " + rideId);
                sendDetailedNotification("New Passenger", "A new passenger has booked your ride");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error in passenger listener: " + error.getMessage());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        };

        // Attach the listener and save it for cleanup
        passengersRef.addChildEventListener(passengerListener);
        listenersMap.put(rideId, passengerListener);
    }

    private void sendDetailedNotification(String title, String content) {
        Log.d(TAG, "Sending notification: " + title + " - " + content);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_account_circle)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }

    private Notification createServiceNotification(String content) {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PickMe")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up all listeners
        for (Map.Entry<String, ChildEventListener> entry : listenersMap.entrySet()) {
            String rideId = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ridesRef.child(rideId).child("Passengers").removeEventListener(listener);
        }

        listenersMap.clear();
        monitoredRides.clear();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}