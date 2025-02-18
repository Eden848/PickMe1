package com.example.pickme;

public class User {

    public String name, age, email;
    private Driver Driver;

    // Constructor with Driver
    public User(String name, String age, String email, Driver Driver) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.Driver = Driver;
    }

    // Constructor without Driver
    public User(String name, String age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.Driver = null;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public Driver getDriver() {
        return Driver;
    }

    // Corrected setter method for the Driver
    public void setDriver(Driver Driver) {
        this.Driver = Driver;
    }
}
