package com.smartcampus.api;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import com.smartcampus.api.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataStore — no HTTP layer, pure Java.
 * Tests the singleton, CRUD operations, and cross-entity linking logic.
 */
class DataStoreTest {

    private DataStore store;

    @BeforeEach
    void setUp() {
        store = DataStore.getInstance();
        store.clear();
    }

    @Test
    void singleton_alwaysReturnsSameInstance() {
        assertSame(DataStore.getInstance(), DataStore.getInstance());
    }

    @Test
    void addRoom_thenGetById_returnsRoom() {
        Room r = new Room("R1", "Lab A", 40);
        store.addRoom(r);
        assertEquals("Lab A", store.getRoomById("R1").getName());
    }

    @Test
    void roomExists_afterAdd_returnsTrue() {
        store.addRoom(new Room("R1", "Lab A", 40));
        assertTrue(store.roomExists("R1"));
    }

    @Test
    void roomExists_beforeAdd_returnsFalse() {
        assertFalse(store.roomExists("GHOST"));
    }

    @Test
    void deleteRoom_removesFromStore() {
        store.addRoom(new Room("R1", "Lab A", 40));
        assertTrue(store.deleteRoom("R1"));
        assertFalse(store.roomExists("R1"));
    }

    @Test
    void deleteRoom_nonExistent_returnsFalse() {
        assertFalse(store.deleteRoom("GHOST"));
    }

    @Test
    void addSensor_linksToRoom() {
        store.addRoom(new Room("R1", "Lab A", 40));
        Sensor s = new Sensor("S1", "temperature", "ACTIVE", 0.0, "R1");
        store.addSensor(s);
        assertTrue(store.getRoomById("R1").getSensorIds().contains("S1"));
    }

    @Test
    void addSensor_initializesReadingsList() {
        store.addRoom(new Room("R1", "Lab A", 40));
        store.addSensor(new Sensor("S1", "temperature", "ACTIVE", 0.0, "R1"));
        assertNotNull(store.getReadingsForSensor("S1"));
        assertEquals(0, store.getReadingsForSensor("S1").size());
    }

    @Test
    void addReading_appendsToSensorList() {
        store.addRoom(new Room("R1", "Lab A", 40));
        store.addSensor(new Sensor("S1", "temperature", "ACTIVE", 0.0, "R1"));
        store.addReading("S1", new SensorReading("RD1", 1713780000L, 26.5));
        store.addReading("S1", new SensorReading("RD2", 1713780001L, 27.0));

        List<SensorReading> readings = store.getReadingsForSensor("S1");
        assertEquals(2, readings.size());
        assertEquals(26.5, readings.get(0).getValue(), 0.001);
        assertEquals(27.0, readings.get(1).getValue(), 0.001);
    }

    @Test
    void clear_removesAllData() {
        store.addRoom(new Room("R1", "Lab A", 40));
        store.addSensor(new Sensor("S1", "temperature", "ACTIVE", 0.0, "R1"));
        store.addReading("S1", new SensorReading("RD1", 1713780000L, 26.5));
        store.clear();

        assertEquals(0, store.getRooms().size());
        assertEquals(0, store.getSensors().size());
        assertEquals(0, store.getReadingsForSensor("S1").size());
    }

    @Test
    void sensorExists_afterAdd_returnsTrue() {
        store.addRoom(new Room("R1", "Lab A", 40));
        store.addSensor(new Sensor("S1", "temperature", "ACTIVE", 0.0, "R1"));
        assertTrue(store.sensorExists("S1"));
    }

    @Test
    void sensorExists_beforeAdd_returnsFalse() {
        assertFalse(store.sensorExists("GHOST"));
    }
}
