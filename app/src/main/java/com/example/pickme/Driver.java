package com.example.pickme;

/**
 * Represents a driver in the PickMe ride-sharing application.
 * This class encapsulates all the necessary information about a driver's
 * ride offer including location details, availability, and associated user information.
 *
 * <p>The Driver class serves as a data model for storing and managing
 * driver-specific information such as pickup location, destination,
 * available seats, and scheduling details.</p>
 *
 * @author PickMe Development Team
 * @version 1.0
 * @since 1.0
 */
public class Driver {

    /**
     * Unique identifier for the driver record.
     * This field stores the unique ID associated with this driver instance.
     */
    private String uid;

    /**
     * Current location of the driver.
     * Represents the starting point or pickup location for the ride.
     */
    private String currentLocation;

    /**
     * Destination location for the ride.
     * Represents where the driver is heading or the drop-off point.
     */
    private String destination;

    /**
     * Number of available seats in the vehicle.
     * Stored as String to accommodate various input formats and validation requirements.
     */
    private String numberOfSeats;

    /**
     * Additional comments or notes about the ride.
     * May include special instructions, vehicle details, or other relevant information.
     */
    private String comment;

    /**
     * Time for the scheduled ride.
     * Stored in string format to accommodate different time formats.
     */
    private String time;

    /**
     * Date for the scheduled ride.
     * Stored in string format to accommodate different date formats.
     */
    private String date;

    /**
     * User object associated with this driver.
     * Contains additional user information linked to the driver profile.
     */
    private User user;

    /**
     * Self-referential driver object.
     * Initialized to null and may be used for specific driver relationships or hierarchies.
     */
    private Driver driver = null;

    /**
     * Default constructor for Driver class.
     * Creates a new Driver instance with all fields set to their default values.
     */
    public Driver() {}

    /**
     * Parameterized constructor for Driver class.
     * Creates a new Driver instance with specified ride details.
     *
     * @param currentLocation the starting location or pickup point
     * @param destination the destination or drop-off location
     * @param numberOfSeats the number of available seats in the vehicle
     * @param comment additional comments or notes about the ride
     * @param date the date for the scheduled ride
     * @param time the time for the scheduled ride
     * @param uid the unique identifier for this driver record
     */
    public Driver(String currentLocation, String destination, String numberOfSeats,
                  String comment, String date, String time, String uid) {
        this.uid = uid;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.numberOfSeats = numberOfSeats;
        this.comment = comment;
        this.time = time;
        this.date = date;
    }

    /**
     * Retrieves the unique identifier for this driver.
     *
     * @return the unique ID as a String, or null if not set
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier for this driver.
     *
     * @param uid the unique identifier to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Retrieves the current location of the driver.
     *
     * @return the current location as a String, or null if not set
     */
    public String getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Sets the current location of the driver.
     *
     * @param currentLocation the current location to set
     */
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * Retrieves the destination location for the ride.
     *
     * @return the destination as a String, or null if not set
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination location for the ride.
     *
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Retrieves the number of available seats.
     *
     * @return the number of seats as a String, or null if not set
     */
    public String getNumberOfSeats() {
        return numberOfSeats;
    }

    /**
     * Sets the number of available seats.
     *
     * @param numberOfSeats the number of seats to set
     */
    public void setNumberOfSeats(String numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    /**
     * Retrieves additional comments about the ride.
     *
     * @return the comment as a String, or null if not set
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets additional comments about the ride.
     *
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retrieves the scheduled time for the ride.
     *
     * @return the time as a String, or null if not set
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the scheduled time for the ride.
     *
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Retrieves the scheduled date for the ride.
     *
     * @return the date as a String, or null if not set
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the scheduled date for the ride.
     *
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Retrieves the User object associated with this driver.
     *
     * @return the associated User object, or null if not set
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the User object associated with this driver.
     *
     * @param user the User object to associate with this driver
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retrieves the Driver object reference.
     * This method returns a self-referential driver object that may be used
     * for specific driver relationships or hierarchical structures.
     *
     * @return the Driver object reference, or null if not set
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Sets the Driver object reference.
     * This method allows setting a self-referential driver object that may be used
     * for specific driver relationships or hierarchical structures.
     *
     * @param driver the Driver object to set as reference
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}