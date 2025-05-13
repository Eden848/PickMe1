package com.example.pickme;

public class Driver {

    private String uid;
    private String currentLocation;
    private String destination;
    private String numberOfSeats;
    private String comment;
    private String time;
    private String date;
    private User user;
    private Driver driver = null;

    public Driver() {}

    public Driver( String currentLocation, String destination, String numberOfSeats, String comment, String date, String time, String uid) {
        this.uid = uid;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.numberOfSeats = numberOfSeats;
        this.comment = comment;
        this.time = time;
        this.date = date;
    }

    // Getter for UID
    public String getUid() {
        return uid;
    }

    // Setter for UID
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getDestination() {
        return destination;
    }

    public String getNumberOfSeats() {
        return numberOfSeats;
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public Driver getDriver() {
        return driver;
    }

    // Setters
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setNumberOfSeats(String numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
