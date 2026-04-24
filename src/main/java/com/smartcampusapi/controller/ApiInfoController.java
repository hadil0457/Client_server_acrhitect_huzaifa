package com.smartcampusapi.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

// discovery endpoint - returns basic info about the api
// clients can hit this to figure out what endpoints are available
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ApiInfoController {

    @GET
    public Map<String, Object> getApiInfo() {
        // using linkedhashmap so the json keys come out in the order i add them
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Smart Campus Sensor & Room Management API");
        info.put("version", "v1");
        info.put("description", "REST API for managing campus rooms, sensors and their readings");
        info.put("contact", "admin@smartcampus.ac.uk");

        // tell the client where the main resource collections are
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");
        info.put("resources", endpoints);

        return info;
    }
}

