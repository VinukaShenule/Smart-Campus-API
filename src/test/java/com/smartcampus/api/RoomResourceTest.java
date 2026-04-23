package com.smartcampus.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for /api/v1/rooms — GET, POST, DELETE.
 */
class RoomResourceTest extends BaseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Helper ──────────────────────────────────────────────────────────────

    private Response createRoom(String id, String name, int capacity) {
        String json = String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"capacity\":%d,\"sensorIds\":[]}", id, name, capacity);
        return target("/rooms").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    // ── POST /rooms ──────────────────────────────────────────────────────────

    @Test
    void createRoom_returns201() {
        Response r = createRoom("R1", "Lab A", 40);
        assertEquals(201, r.getStatus());
    }

    @Test
    void createRoom_responseBodyContainsId() {
        String body = createRoom("R1", "Lab A", 40).readEntity(String.class);
        assertTrue(body.contains("R1"));
    }

    @Test
    void createRoom_duplicateId_returns409() {
        createRoom("R1", "Lab A", 40);
        Response r = createRoom("R1", "Lab B", 20);
        assertEquals(409, r.getStatus());
    }

    @Test
    void createRoom_missingId_returns400() {
        String json = "{\"name\":\"Lab A\",\"capacity\":40,\"sensorIds\":[]}";
        Response r = target("/rooms").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
        assertEquals(400, r.getStatus());
    }

    // ── GET /rooms ───────────────────────────────────────────────────────────

    @Test
    void getAllRooms_emptyStore_returns200AndEmptyArray() throws Exception {
        Response r = target("/rooms").request().get();
        assertEquals(200, r.getStatus());
        JsonNode body = MAPPER.readTree(r.readEntity(String.class));
        assertTrue(body.isArray());
        assertEquals(0, body.size());
    }

    @Test
    void getAllRooms_afterInsert_returnsOneRoom() throws Exception {
        createRoom("R1", "Lab A", 40);
        Response r = target("/rooms").request().get();
        JsonNode body = MAPPER.readTree(r.readEntity(String.class));
        assertEquals(1, body.size());
    }

    @Test
    void getAllRooms_multipleRooms_returnsAll() throws Exception {
        createRoom("R1", "Lab A", 40);
        createRoom("R2", "Lab B", 30);
        createRoom("R3", "Hall", 100);
        Response r = target("/rooms").request().get();
        JsonNode body = MAPPER.readTree(r.readEntity(String.class));
        assertEquals(3, body.size());
    }

    // ── GET /rooms/{id} ──────────────────────────────────────────────────────

    @Test
    void getRoomById_exists_returns200() {
        createRoom("R1", "Lab A", 40);
        Response r = target("/rooms/R1").request().get();
        assertEquals(200, r.getStatus());
    }

    @Test
    void getRoomById_exists_returnsCorrectName() {
        createRoom("R1", "Lab A", 40);
        String body = target("/rooms/R1").request().get(String.class);
        assertTrue(body.contains("Lab A"));
    }

    @Test
    void getRoomById_notFound_returns404() {
        Response r = target("/rooms/DOES_NOT_EXIST").request().get();
        assertEquals(404, r.getStatus());
    }

    @Test
    void getRoomById_notFound_bodyHasErrorMessage() {
        Response r = target("/rooms/DOES_NOT_EXIST").request().get();
        String body = r.readEntity(String.class);
        assertTrue(body.contains("message") || body.contains("error"));
    }

    // ── DELETE /rooms/{id} ───────────────────────────────────────────────────

    @Test
    void deleteRoom_noSensors_returns200() {
        createRoom("R1", "Lab A", 40);
        Response r = target("/rooms/R1").request().delete();
        assertEquals(200, r.getStatus());
    }

    @Test
    void deleteRoom_thenGet_returns404() {
        createRoom("R1", "Lab A", 40);
        target("/rooms/R1").request().delete();
        Response r = target("/rooms/R1").request().get();
        assertEquals(404, r.getStatus());
    }

    @Test
    void deleteRoom_notFound_returns404() {
        Response r = target("/rooms/GHOST").request().delete();
        assertEquals(404, r.getStatus());
    }

    @Test
    void deleteRoom_withSensors_returns409() {
        // Create room then attach a sensor to it
        createRoom("R1", "Lab A", 40);
        String sensorJson = "{\"id\":\"S1\",\"type\":\"temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R1\"}";
        target("/sensors").request()
                .post(Entity.entity(sensorJson, MediaType.APPLICATION_JSON));

        Response r = target("/rooms/R1").request().delete();
        assertEquals(409, r.getStatus());
    }

    @Test
    void deleteRoom_withSensors_errorBodyMentionsConflict() {
        createRoom("R1", "Lab A", 40);
        String sensorJson = "{\"id\":\"S1\",\"type\":\"temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R1\"}";
        target("/sensors").request()
                .post(Entity.entity(sensorJson, MediaType.APPLICATION_JSON));
        Response r = target("/rooms/R1").request().delete();
        String body = r.readEntity(String.class);
        assertTrue(body.toLowerCase().contains("sensor") || body.toLowerCase().contains("conflict"));
    }
}
