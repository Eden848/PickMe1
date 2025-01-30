package com.example.pickme;

public class Passenger {
    private String currentLocation;
    private String destination;
    private String numberOfPassengers;
    private String comment;

    // Empty constructor for Firebase
    public Passenger() {}

    // Constructor with parameters
    public Passenger(String currentLocation, String destination, String numberOfPassengers, String comment) {
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.numberOfPassengers = numberOfPassengers;
        this.comment = comment;
    }

    // Getters and Setters
    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(String numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
