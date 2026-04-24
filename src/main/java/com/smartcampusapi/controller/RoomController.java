package com.smartcampusapi.controller;

import com.smartcampusapi.data.CampusData;
import com.smartcampusapi.dto.Room;
import com.smartcampusapi.errors.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// handles everything to do with rooms
// GET all rooms, GET single room, POST new room, DELETE room
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomController {

    // return every room we have stored
    @GET
    public List<Room> fetchAllRooms() {
        return new ArrayList<>(CampusData.getAllRooms().values());
    }

    // look up a single room by its id
    @GET
    @Path("/{id}")
    public Response fetchRoomById(@PathParam("id") String id) {
        Room room = CampusData.getRoom(id);
        if (room == null) {
            // room doesnt exist, send back 404 with a helpful message
            Map<String, String> err = new LinkedHashMap<>();
            err.put("status", "404");
            err.put("message", "could not find a room with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    // add a new room to the system
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isEmpty()) {
            Map<String, String> err = new LinkedHashMap<>();
            err.put("status", "400");
            err.put("message", "you need to provide a room with a valid id");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        CampusData.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // delete a room but only if theres no sensors still in it
    // if there are sensors we throw RoomNotEmptyException which gets mapped to 409
    @DELETE
    @Path("/{id}")
    public Response removeRoom(@PathParam("id") String id) {
        Room room = CampusData.getRoom(id);
        if (room == null) {
            Map<String, String> err = new LinkedHashMap<>();
            err.put("status", "404");
            err.put("message", "no room exists with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        // safety check - cant delete a room if sensors are still linked to it
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "room " + id + " has " + room.getSensorIds().size()
                + " sensor(s) still inside it, remove them first before deleting"
            );
        }

        CampusData.removeRoom(id);
        // 204 = deleted successfully, no body needed
        return Response.noContent().build();
    }
}

