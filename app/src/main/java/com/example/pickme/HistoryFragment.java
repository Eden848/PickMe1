package com.example.pickme;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private Button btnDriverHistory, btnPassengerHistory;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<Driver> driverList;
    private List<Passenger> passengerList;
    private boolean showingDriver = true;

    private String currentUserUid;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverList = new ArrayList<>();
        passengerList = new ArrayList<>();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        btnDriverHistory = view.findViewById(R.id.btnDriverHistory);
        btnPassengerHistory = view.findViewById(R.id.btnPassengerHistory);
        recyclerView = view.findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter();
        adapter.setDriverData(driverList);      // when showing driver history
        adapter.setPassengerData(passengerList); // when showing passenger history

        recyclerView.setAdapter(adapter);

        btnDriverHistory.setOnClickListener(v -> {
            showingDriver = true;
            loadDriverHistory();
        });

        btnPassengerHistory.setOnClickListener(v -> {
            showingDriver = false;
            loadPassengerHistory();
        });

        // Load default
        loadDriverHistory();

        return view;
    }

    private void loadDriverHistory() {
        driverList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("HistoryDriver").child(currentUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Driver driver = rideSnapshot.getValue(Driver.class);
                    driverList.add(driver);
                }
                adapter.setShowingDriver(true);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load driver history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPassengerHistory() {
        passengerList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("HistoryPassenger").child(currentUserUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Passenger passenger = rideSnapshot.getValue(Passenger.class);
                    passengerList.add(passenger);
                }
                adapter.setShowingDriver(false);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load passenger history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
