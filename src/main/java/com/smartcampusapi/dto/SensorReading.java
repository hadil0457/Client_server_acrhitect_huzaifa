package com.smartcampusapi.dto;

import java.util.UUID;

// single reading from a sensor at a point in time
// id is auto generated using uuid so we dont have to worry about collisions
public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {
        // for jackson
    }

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}

