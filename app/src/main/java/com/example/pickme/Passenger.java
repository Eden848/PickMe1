package com.example.pickme;

/**
 * Represents a passenger in the PickMe ride-sharing application.
 * This class serves as a data model for storing passenger information including
 * their current location, destination, number of passengers, and additional comments.
 *
 * The class is designed to work seamlessly with Firebase Realtime Database,
 * providing both parameterized and empty constructors for proper serialization
 * and deserialization of passenger data.
 *
 * <p>Usage Example:</p>
 * <pre>
 * {@code
 * Passenger passenger = new Passenger(
 *     "Downtown Plaza",
 *     "Airport Terminal 1",
 *     "2",
 *     "Please wait at the main entrance"
 * );
 * }
 * </pre>
 *
 * @author Your Name
 * @version 1.0
 * @since API Level 21
 */
public class Passenger {

    /** The passenger's current pickup location */
    private String currentLocation;

    /** The passenger's desired destination */
    private String destination;

    /** The total number of passengers for this ride request (as string for flexibility) */
    private String numberOfPassengers;

    /** Additional comments or special instructions from the passenger */
    private String comment;

    /**
     * Default empty constructor required for Firebase Realtime Database deserialization.
     * Firebase uses reflection to create instances of this class when reading data
     * from the database, requiring a no-argument constructor.
     *
     * <p>This constructor initializes all fields to null by default.</p>
     *
     * @see <a href="https://firebase.google.com/docs/database/android/read-and-write#basic_write">Firebase Documentation</a>
     */
    public Passenger() {
        // Empty constructor for Firebase serialization/deserialization
    }

    /**
     * Constructs a new Passenger with the specified ride details.
     * This constructor is used when creating a new passenger request with all
     * necessary information provided by the user.
     *
     * @param currentLocation The passenger's current pickup location.
     *                       Should not be null or empty for valid ride requests.
     * @param destination The passenger's desired destination.
     *                   Should not be null or empty for valid ride requests.
     * @param numberOfPassengers The total number of passengers for this ride.
     *                          Stored as String for flexibility in handling various input formats.
     *                          Should represent a positive integer value.
     * @param comment Additional comments or special instructions from the passenger.
     *               Can be null or empty if no special instructions are needed.
     *
     * @throws IllegalArgumentException if currentLocation or destination is null
     */
    public Passenger(String currentLocation, String destination, String numberOfPassengers, String comment) {
        if (currentLocation == null) {
            throw new IllegalArgumentException("Current location cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        }

        this.currentLocation = currentLocation;
        this.destination = destination;
        this.numberOfPassengers = numberOfPassengers;
        this.comment = comment;
    }

    /**
     * Retrieves the passenger's current pickup location.
     *
     * @return The current location as a String, or null if not set
     */
    public String getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Sets the passenger's current pickup location.
     * This method is used to update the pickup location after the initial
     * passenger object creation or when loading data from Firebase.
     *
     * @param currentLocation The new current location. Should not be null for valid requests.
     * @throws IllegalArgumentException if currentLocation is null
     */
    public void setCurrentLocation(String currentLocation) {
        if (currentLocation == null) {
            throw new IllegalArgumentException("Current location cannot be null");
        }
        this.currentLocation = currentLocation;
    }

    /**
     * Retrieves the passenger's desired destination.
     *
     * @return The destination as a String, or null if not set
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the passenger's desired destination.
     * This method is used to update the destination after the initial
     * passenger object creation or when loading data from Firebase.
     *
     * @param destination The new destination. Should not be null for valid requests.
     * @throws IllegalArgumentException if destination is null
     */
    public void setDestination(String destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        }
        this.destination = destination;
    }

    /**
     * Retrieves the number of passengers for this ride request.
     *
     * @return The number of passengers as a String, or null if not set.
     *         The string should represent a positive integer value.
     */
    public String getNumberOfPassengers() {
        return numberOfPassengers;
    }

    /**
     * Sets the number of passengers for this ride request.
     * The number is stored as a String to provide flexibility in handling
     * various input formats and to maintain consistency with Firebase storage.
     *
     * @param numberOfPassengers The number of passengers as a String.
     *                          Should represent a positive integer value for valid requests.
     *                          Can be null if not specified.
     */
    public void setNumberOfPassengers(String numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    /**
     * Retrieves additional comments or special instructions from the passenger.
     *
     * @return The comment as a String, or null if no comment was provided
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets additional comments or special instructions for the ride request.
     * Comments can include information such as:
     * - Special pickup instructions
     * - Accessibility requirements
     * - Preferred route suggestions
     * - Contact preferences
     *
     * @param comment The comment or special instructions. Can be null or empty
     *               if no additional information is needed.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns a string representation of the Passenger object.
     * This method is useful for debugging and logging purposes.
     *
     * @return A formatted string containing all passenger information
     */
    @Override
    public String toString() {
        return "Passenger{" +
                "currentLocation='" + currentLocation + '\'' +
                ", destination='" + destination + '\'' +
                ", numberOfPassengers='" + numberOfPassengers + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    /**
     * Validates whether this passenger object contains all required information
     * for a valid ride request.
     *
     * @return true if the passenger has valid current location, destination,
     *         and number of passengers; false otherwise
     */
    public boolean isValidRequest() {
        return currentLocation != null && !currentLocation.trim().isEmpty() &&
                destination != null && !destination.trim().isEmpty() &&
                numberOfPassengers != null && !numberOfPassengers.trim().isEmpty();
    }

    /**
     * Checks if this passenger request has additional comments.
     *
     * @return true if comment is not null and not empty, false otherwise
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}