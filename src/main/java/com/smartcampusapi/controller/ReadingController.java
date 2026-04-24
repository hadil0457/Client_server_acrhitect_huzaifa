package com.smartcampusapi.controller;

import com.smartcampusapi.data.CampusData;
import com.smartcampusapi.dto.Sensor;
import com.smartcampusapi.dto.SensorReading;
import com.smartcampusapi.errors.SensorUnavailableException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

// sub-resource class for handling sensor readings
// this gets instantiated by SensorController's sub-resource locator method
// so it always knows which sensor its working with
public class ReadingController {

    private final String sensorId;

    // sensorId is passed in from the parent controller
    public ReadingController(String sensorId) {
        this.sensorId = sensorId;
    }

    // get all historical readings for this particular sensor
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getAllReadings() {
        return CampusData.getReadings(sensorId);
    }

    // post a new reading for this sensor
    // also updates the parent sensor's currentValue to keep things in sync
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // check if the sensor is in maintenance mode - if so we cant accept readings
        Sensor parentSensor = CampusData.getSensor(sensorId);
        if (parentSensor != null && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(
                "sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings"
            );
        }

        // auto generate id and timestamp if the client didnt provide them
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // save the reading
        CampusData.addReading(sensorId, reading);

        // important: update the sensor's current value to match this latest reading
        // this keeps the sensor object consistent with the most recent data
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}

