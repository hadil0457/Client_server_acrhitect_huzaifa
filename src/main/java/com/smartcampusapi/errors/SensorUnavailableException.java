package com.smartcampusapi.errors;

// thrown when trying to post a reading to a sensor thats in MAINTENANCE mode
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String msg) {
        super(msg);
    }
}

