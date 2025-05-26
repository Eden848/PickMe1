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

/**
 * {@code RideMonitorService} is an Android Service that runs in the background to monitor
 * Firebase Realtime Database for changes related to a user's rides, specifically looking for
 * new passenger bookings. When a new passenger is detected for a ride owned by the current user,
 * it sends a notification to the user. This service runs as a foreground service to ensure
 * it continues operating even when the app is not in the foreground.
 */
public class RideMonitorService extends Service {
    /**
     * Tag for logging messages related to this service.
     */
    private static final String TAG = "RideMonitorService";
    /**
     * The ID for the notification channel used by this service.
     */
    private static final String CHANNEL_ID = "ride_notifications";
    /**
     * The unique ID for the foreground service notification.
     */
    private static final int NOTIFICATION_ID = 1;
    /**
     * DatabaseReference to the "Rides" node in Firebase.
     */
    private DatabaseReference ridesRef;
    /**
     * A map to store ChildEventListener instances, keyed by ride ID, for cleanup.
     */
    private Map<String, ChildEventListener> listenersMap = new HashMap<>();
    /**
     * A set to keep track of ride IDs that are currently being monitored by the service.
     */
    private Set<String> monitoredRides = new HashSet<>();

    /**
     * Called by the system when the service is first created. This method is used for one-time setup.
     * It initializes Firebase references, creates the notification channel, starts the service
     * in the foreground, and begins listening for ride changes.
     */
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        ridesRef = FirebaseDatabase.getInstance().getReference("Rides");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createServiceNotification("Monitoring Rides..."));
        startListeningForRides();
    }

    /**
     * Creates a notification channel for Android Oreo (API 26) and above.
     * This channel is used to categorize notifications sent by this service.
     */
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

    /**
     * Starts listening for new rides or changes to existing rides in Firebase.
     * It attaches a {@link ChildEventListener} to the "Rides" node.
     * When a new ride is added, it checks if the ride belongs to the current user.
     * If it does, a separate listener is attached to that ride's "Passengers" node.
     * It also handles the removal of ride listeners when a ride is deleted.
     */
    private void startListeningForRides() {
        Log.i(TAG, "startListeningForRides/method started");
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            Log.e(TAG, "No user logged in");
            stopSelf(); // Stop the service if no user is logged in
            return;
        }

        String uid = fbUser.getUid();

        // Listen to all rides
        ridesRef.addChildEventListener(new ChildEventListener() {
            /**
             * Called when a new child is added to the "Rides" node.
             * It checks if the newly added ride belongs to the current user and, if so,
             * attaches a passenger listener to it.
             * @param rideSnapshot The DataSnapshot containing the new ride data.
             * @param previousChildName The key of the previous sibling child, or null if this is the first child.
             */
            @Override
            public void onChildAdded(@NonNull DataSnapshot rideSnapshot, @Nullable String previousChildName) {
                String rideId = rideSnapshot.getKey();
                Log.i(TAG, "Found ride: " + rideId);

                // Check if this ride belongs to the current user by checking the uid field
                if (rideSnapshot.hasChild("uid")) {
                    String rideUid = rideSnapshot.child("uid").getValue(String.class);
                    if (uid.equals(rideUid)) {
                        Log.i(TAG, "This ride belongs to the current user: " + rideId);
                        monitoredRides.add(rideId); // Add to set of monitored rides
                        attachPassengerListener(rideId); // Attach a specific listener for passengers on this ride
                    }
                }
            }

            /**
             * Called when the data at a child location is updated. Not used for primary logic in this service.
             * @param snapshot The DataSnapshot containing the updated data.
             * @param previousChildName The key of the previous sibling child.
             */
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            /**
             * Called when a child is removed from the "Rides" node.
             * It removes the corresponding passenger listener and the ride from the monitored set.
             * @param snapshot The DataSnapshot containing the data of the removed child.
             */
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String rideId = snapshot.getKey();
                if (monitoredRides.contains(rideId)) {
                    Log.i(TAG, "Removing listeners for ride: " + rideId);
                    if (listenersMap.containsKey(rideId)) {
                        // Remove the specific passenger listener for this ride
                        ridesRef.child(rideId).child("Passengers").removeEventListener(listenersMap.get(rideId));
                        listenersMap.remove(rideId); // Remove from the map
                    }
                    monitoredRides.remove(rideId); // Remove from the set of monitored rides
                }
            }

            /**
             * Called when a Firebase database operation is cancelled.
             * @param error The DatabaseError object indicating the reason for the cancellation.
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error in ride listener: " + error.getMessage());
            }

            /**
             * Called when a child is moved to a new position in the list. Not used for primary logic in this service.
             * @param snapshot The DataSnapshot containing the data of the moved child.
             * @param previousChildName The key of the previous sibling child.
             */
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        });
    }

    /**
     * Attaches a {@link ChildEventListener} to the "Passengers" node of a specific ride.
     * This listener is responsible for detecting new passenger additions to that ride
     * and sending a notification. It also performs an initial check for existing passengers.
     *
     * @param rideId The ID of the ride to monitor for new passengers.
     */
    private void attachPassengerListener(String rideId) {
        Log.i(TAG, "Attaching passenger listener for ride: " + rideId);

        // The path should be: Rides -> RideID -> Passengers
        DatabaseReference passengersRef = ridesRef.child(rideId).child("Passengers");

        // First, check if there are already passengers when this listener is attached
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

        // Now listen for new passengers being added
        ChildEventListener passengerListener = new ChildEventListener() {
            /**
             * Called when a new passenger is added to the "Passengers" node of the monitored ride.
             * Sends a notification to the user.
             * @param snapshot The DataSnapshot containing the new passenger data.
             * @param previousChildName The key of the previous sibling child, or null if this is the first child.
             */
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i(TAG, "New passenger added for ride: " + rideId);
                sendDetailedNotification("New Passenger", "A new passenger has booked your ride");
            }

            /**
             * Called when the data at a passenger child location is updated. Not used in this context.
             * @param snapshot The DataSnapshot containing the updated data.
             * @param previousChildName The key of the previous sibling child.
             */
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            /**
             * Called when a passenger child is removed. Not used in this context.
             * @param snapshot The DataSnapshot containing the data of the removed child.
             */
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            /**
             * Called when a Firebase database operation is cancelled for the passenger listener.
             * @param error The DatabaseError object indicating the reason for the cancellation.
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error in passenger listener: " + error.getMessage());
            }

            /**
             * Called when a passenger child is moved to a new position. Not used in this context.
             * @param snapshot The DataSnapshot containing the data of the moved child.
             * @param previousChildName The key of the previous sibling child.
             */
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        };

        // Attach the listener to the passengers reference and save it in the map for later cleanup
        passengersRef.addChildEventListener(passengerListener);
        listenersMap.put(rideId, passengerListener);
    }

    /**
     * Sends a detailed notification to the user.
     * The notification includes a title, content text, a small icon, and a tap action
     * that opens the {@link HomeActivity}.
     *
     * @param title The title of the notification.
     * @param content The main text content of the notification.
     */
    private void sendDetailedNotification(String title, String content) {
        Log.d(TAG, "Sending notification: " + title + " - " + content);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_account_circle) // Small icon for the notification
                .setContentTitle(title) // Title of the notification
                .setContentText(content) // Content text of the notification
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for important alerts
                .setAutoCancel(true); // Notification dismisses when tapped

        // Create an Intent to open HomeActivity when the notification is tapped
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE); // FLAG_IMMUTABLE is required for API 31+
        builder.setContentIntent(pendingIntent); // Set the intent for the notification

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build()); // Send the notification with a unique ID
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }

    /**
     * Creates a {@link Notification} object to be used for the foreground service.
     * This notification informs the user that the service is running in the background.
     *
     * @param content The content text to display in the service notification.
     * @return A Notification object for the foreground service.
     */
    private Notification createServiceNotification(String content) {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE is required for API 31+
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PickMe") // Title of the foreground notification
                .setContentText(content) // Content text (e.g., "Monitoring Rides...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Small icon for the foreground notification
                .setContentIntent(pendingIntent) // Intent to open HomeActivity
                .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority for ongoing background service
                .build();
    }

    /**
     * Called by the system every time a client starts the service using {@link Context#startService}.
     * This implementation returns {@link Service#START_STICKY}, indicating that the system
     * should try to re-create the service if it gets killed.
     *
     * @param intent The Intent supplied to {@link Context#startService}, as given by the client.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's
     * current started state.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     * This method is used to clean up all Firebase listeners to prevent memory leaks and
     * ensure proper resource management.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up all attached ChildEventListeners
        for (Map.Entry<String, ChildEventListener> entry : listenersMap.entrySet()) {
            String rideId = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ridesRef.child(rideId).child("Passengers").removeEventListener(listener);
        }

        listenersMap.clear(); // Clear the map of listeners
        monitoredRides.clear(); // Clear the set of monitored rides
    }

    /**
     * Called when a client is binding to the service, using {@link Context#bindService}.
     * This service does not support binding, so it returns null.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return An IBinder through which clients can communicate with the service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
