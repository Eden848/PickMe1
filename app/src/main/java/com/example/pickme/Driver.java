package com.example.pickme;

public class Driver {

    private String currentLocation;
    private String destination;
    private String numberOfSeats;
    private String comment;
    private User user;


    public Driver() {}

    public Driver(String currentLocation, String destination, String numberOfSeats, String comment) {
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.numberOfSeats = numberOfSeats;
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


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


    public String getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(String numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

