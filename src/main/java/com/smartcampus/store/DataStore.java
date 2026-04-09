package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory data store using thread-safe ConcurrentHashMap.
 * Shared across all resource classes as a singleton via static fields.
 */
public class DataStore {

    // Room storage: roomId -> Room
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Sensor storage: sensorId -> Sensor
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Sensor readings storage: sensorId -> List of SensorReading
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // --- Room operations ---

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static Room removeRoom(String roomId) {
        return rooms.remove(roomId);
    }

    // --- Sensor operations ---

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Sensor getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        sensorReadings.put(sensor.getId(), new ArrayList<>());
    }

    // --- Sensor Reading operations ---

    public static List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
