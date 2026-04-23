package com.smartcampus.api.store;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 * Uses ConcurrentHashMap to handle concurrent requests safely.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    // key = sensorId, value = list of readings for that sensor
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // --- ROOM OPERATIONS ---

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // --- SENSOR OPERATIONS ---

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Also register sensor in room's sensorIds list
        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        // Init readings list for this sensor
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // --- READING OPERATIONS ---

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    /**
     * Clears all data. Used only in unit tests to reset state between test cases.
     */
    public void clear() {
        rooms.clear();
        sensors.clear();
        readings.clear();
    }

}
