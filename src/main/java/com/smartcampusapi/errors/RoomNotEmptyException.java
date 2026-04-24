package com.smartcampusapi.errors;

// thrown when someone tries to delete a room that still has sensors in it
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String msg) {
        super(msg);
    }
}

