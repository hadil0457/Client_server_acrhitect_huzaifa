package com.smartcampusapi.data;

import com.smartcampusapi.dto.Room;
import com.smartcampusapi.dto.Sensor;
import com.smartcampusapi.dto.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// basically our fake database lol
// everything is stored in ConcurrentHashMaps so its thread safe
// all methods are static because we only ever need one copy of the data
public class CampusData {

    // using concurrent hashmaps so multiple requests dont mess things up
    private static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // dont let anyone instantiate this, its purely static
    private CampusData() {}

    // ---- room operations ----

    public static ConcurrentHashMap<String, Room> getAllRooms() {
        return rooms;
    }

    public static Room getRoom(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static Room removeRoom(String id) {
        return rooms.remove(id);
    }

    public static boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ---- sensor operations ----

    public static ConcurrentHashMap<String, Sensor> getAllSensors() {
        return sensors;
    }

    public static Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // also initialize an empty readings list for this new sensor
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    // ---- reading operations ----

    public static List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}

