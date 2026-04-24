package com.smartcampusapi.controller;

import com.smartcampusapi.data.CampusData;
import com.smartcampusapi.dto.Room;
import com.smartcampusapi.dto.Sensor;
import com.smartcampusapi.errors.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// manages sensors - creating them, listing them, filtering by type
// also has the sub-resource locator that connects to ReadingController
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorController {

    // get all sensors, can optionally filter by type using ?type=Temperature etc
    @GET
    public List<Sensor> fetchSensors(@QueryParam("type") String type) {
        List<Sensor> all = new ArrayList<>(CampusData.getAllSensors().values());

        if (type != null && !type.trim().isEmpty()) {
            // filter by type if the query param was provided
            all = all.stream()
                    .filter(s -> type.equalsIgnoreCase(s.getType()))
                    .collect(Collectors.toList());
        }
        return all;
    }

    // register a new sensor in the system
    // the roomId in the request body has to point to an actual room that exists
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isEmpty()) {
            Map<String, String> err = new LinkedHashMap<>();
            err.put("status", "400");
            err.put("message", "sensor must have a valid id");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        // check that the room this sensor is supposed to go in actually exists
        Room targetRoom = CampusData.getRoom(sensor.getRoomId());
        if (targetRoom == null) {
            throw new LinkedResourceNotFoundException(
                "cant register sensor '" + sensor.getId()
                + "' because room '" + sensor.getRoomId() + "' doesnt exist in the system"
            );
        }

        // save the sensor
        CampusData.addSensor(sensor);
        // link it to the room as well
        targetRoom.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // sub-resource locator for readings
    // this doesnt have a http method annotation on purpose - thats how sub-resource locators work
    // jersey calls this method, gets the ReadingController object, then dispatches GET/POST to it
    @Path("/{sensorId}/readings")
    public ReadingController getReadingsController(@PathParam("sensorId") String sensorId) {
        // first check the sensor actually exists
        if (CampusData.getSensor(sensorId) == null) {
            throw new NotFoundException("sensor with id '" + sensorId + "' was not found");
        }
        return new ReadingController(sensorId);
    }
}

