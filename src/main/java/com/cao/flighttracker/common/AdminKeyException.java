package com.cao.flighttracker.common;

public class AdminKeyException extends RuntimeException {

    public AdminKeyException() {
        super("Missing or invalid admin key");
    }
}