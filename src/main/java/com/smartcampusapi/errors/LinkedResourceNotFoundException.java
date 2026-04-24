package com.smartcampusapi.errors;

// thrown when a sensor references a roomId that doesnt actually exist
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String msg) {
        super(msg);
    }
}

