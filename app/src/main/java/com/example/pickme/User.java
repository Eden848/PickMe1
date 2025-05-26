package com.example.pickme;

/**
 * The {@code User} class represents a user in the PickMe application.
 * It stores basic user information such as name, age, and email.
 * It can also optionally associate a {@link Driver} object with the user,
 * indicating if the user is currently acting as a driver or has driver-related data.
 */
public class User {

    /**
     * The name of the user.
     */
    public String name;
    /**
     * The age of the user.
     */
    public String age;
    /**
     * The email address of the user.
     */
    public String email;
    /**
     * An optional {@link Driver} object associated with the user.
     * This field is used if the user is a driver or has driver-specific details.
     */
    private Driver Driver; // Note: Field name is "Driver" (capital D) as in original code

    /**
     * Constructs a new {@code User} object with specified name, age, email, and an associated driver.
     * This constructor is typically used when creating a user who is also a driver or has driver-related data.
     *
     * @param name The name of the user.
     * @param age The age of the user.
     * @param email The email address of the user.
     * @param Driver The {@link Driver} object associated with this user.
     */
    public User(String name, String age, String email, Driver Driver) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.Driver = Driver;
    }

    /**
     * Constructs a new {@code User} object with specified name, age, and email,
     * without an associated driver. The {@code Driver} field will be set to {@code null}.
     * This constructor is typically used for general users or passengers.
     *
     * @param name The name of the user.
     * @param age The age of the user.
     * @param email The email address of the user.
     */
    public User(String name, String age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.Driver = null;
    }

    // Getter methods
    /**
     * Returns the name of the user.
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the age of the user.
     * @return The user's age.
     */
    public String getAge() {
        return age;
    }

    /**
     * Returns the email address of the user.
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the {@link Driver} object associated with this user.
     * @return The associated {@link Driver} object, or {@code null} if no driver is associated.
     */
    public Driver getDriver() {
        return Driver;
    }

    // Setter method for the Driver
    /**
     * Sets the {@link Driver} object associated with this user.
     * @param Driver The {@link Driver} object to set.
     */
    public void setDriver(Driver Driver) {
        this.Driver = Driver;
    }
}
