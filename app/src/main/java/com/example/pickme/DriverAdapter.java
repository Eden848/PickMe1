package com.example.pickme;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private List<Driver> driverList;
    private Context context;

    public DriverAdapter(List<Driver> driverList, Context context) {
        this.driverList = driverList;
        this.context = context;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        User user = driver.getUser();

        if (user != null) {
            holder.tvNameAge.setText(user.getName() + ", " + user.getAge());
        } else {
            holder.tvNameAge.setText("Unknown Driver");
        }


        holder.locationTextView.setText("From: " + driver.getCurrentLocation());
        holder.destinationTextView.setText("To: " + driver.getDestination());
        holder.seatsTextView.setText("Seats: " + driver.getNumberOfSeats());
        holder.commentTextView.setText("Comment: " + driver.getComment());
        holder.dateText.setText("Date: " + driver.getDate());
        holder.timeText.setText("Time: " +  driver.getTime());

        holder.orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("Users");
                DatabaseReference ridesRef = database.getReference("Rides");

                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user UID
                String driverUid = driver.getUid(); // Get selected driver UID

                usersRef.child(currentUserUid).child("Passenger").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Get passenger data
                        Map<String, Object> passengerData = (Map<String, Object>) task.getResult().getValue();

                        // Save passenger data under Rides/{driverUid}/passengers/{currentUserUid}
                        ridesRef.child(driverUid).child("Passengers").child(currentUserUid).setValue(passengerData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Ride booked successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to book ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "No passenger data found!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView, destinationTextView, seatsTextView, dateText, timeText, commentTextView, tvNameAge;
        Button orderButton;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameAge = itemView.findViewById(R.id.tv_name_age);
            locationTextView = itemView.findViewById(R.id.textLocation);
            destinationTextView = itemView.findViewById(R.id.textDestination);
            seatsTextView = itemView.findViewById(R.id.textSeats);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            commentTextView = itemView.findViewById(R.id.textComment);
            orderButton = itemView.findViewById(R.id.btn_order_driver);
        }
    }
}