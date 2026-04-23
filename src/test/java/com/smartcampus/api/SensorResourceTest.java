package com.smartcampus.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for /api/v1/sensors — GET, POST, filtered GET.
 */
class SensorResourceTest extends BaseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void createRoom(String id) {
        String json = String.format(
            "{\"id\":\"%s\",\"name\":\"Room %s\",\"capacity\":30,\"sensorIds\":[]}", id, id);
        target("/rooms").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    private Response createSensor(String id, String type, String status, String roomId) {
        String json = String.format(
            "{\"id\":\"%s\",\"type\":\"%s\",\"status\":\"%s\",\"currentValue\":0,\"roomId\":\"%s\"}",
            id, type, status, roomId);
        return target("/sensors").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    // ── POST /sensors ─────────────────────────────────────────────────────────

    @Test
    void createSensor_validRoom_returns201() {
        createRoom("R1");
        Response r = createSensor("S1", "temperature", "ACTIVE", "R1");
        assertEquals(201, r.getStatus());
    }

    @Test
    void createSensor_responseBodyContainsId() {
        createRoom("R1");
        String body = createSensor("S1", "temperature", "ACTIVE", "R1").readEntity(String.class);
        assertTrue(body.contains("S1"));
    }

    @Test
    void createSensor_invalidRoomId_returns422() {
        Response r = createSensor("S1", "temperature", "ACTIVE", "NONEXISTENT");
        assertEquals(422, r.getStatus());
    }

    @Test
    void createSensor_invalidRoomId_errorBodyMentionsRoom() {
        String body = createSensor("S1", "temperature", "ACTIVE", "NONEXISTENT")
                .readEntity(String.class);
        assertTrue(body.toLowerCase().contains("room"));
    }

    @Test
    void createSensor_duplicateId_returns409() {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");
        Response r = createSensor("S1", "humidity", "ACTIVE", "R1");
        assertEquals(409, r.getStatus());
    }

    @Test
    void createSensor_missingId_returns400() {
        createRoom("R1");
        String json = "{\"type\":\"temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R1\"}";
        Response r = target("/sensors").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
        assertEquals(400, r.getStatus());
    }

    @Test
    void createSensor_registersInRoom_roomSensorIdsUpdated() throws Exception {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");
        String body = target("/rooms/R1").request().get(String.class);
        assertTrue(body.contains("S1"), "Room's sensorIds should include S1 after sensor creation");
    }

    // ── GET /sensors ──────────────────────────────────────────────────────────

    @Test
    void getAllSensors_emptyStore_returns200EmptyArray() throws Exception {
        Response r = target("/sensors").request().get();
        assertEquals(200, r.getStatus());
        JsonNode body = MAPPER.readTree(r.readEntity(String.class));
        assertTrue(body.isArray());
        assertEquals(0, body.size());
    }

    @Test
    void getAllSensors_afterInsert_returnsOne() throws Exception {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");
        JsonNode body = MAPPER.readTree(target("/sensors").request().get(String.class));
        assertEquals(1, body.size());
    }

    // ── GET /sensors?type= ────────────────────────────────────────────────────

    @Test
    void getSensorsByType_matchingType_returnsFiltered() throws Exception {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");
        createSensor("S2", "humidity", "ACTIVE", "R1");
        createSensor("S3", "temperature", "ACTIVE", "R1");

        JsonNode body = MAPPER.readTree(
            target("/sensors").queryParam("type", "temperature").request().get(String.class));
        assertEquals(2, body.size());
        for (JsonNode node : body) {
            assertEquals("temperature", node.get("type").asText());
        }
    }

    @Test
    void getSensorsByType_noMatch_returnsEmptyArray() throws Exception {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");

        JsonNode body = MAPPER.readTree(
            target("/sensors").queryParam("type", "co2").request().get(String.class));
        assertEquals(0, body.size());
    }

    @Test
    void getSensorsByType_caseInsensitive_returnsMatch() throws Exception {
        createRoom("R1");
        createSensor("S1", "Temperature", "ACTIVE", "R1");

        JsonNode body = MAPPER.readTree(
            target("/sensors").queryParam("type", "TEMPERATURE").request().get(String.class));
        assertEquals(1, body.size());
    }

    // ── GET /sensors/{id} ─────────────────────────────────────────────────────

    @Test
    void getSensorById_exists_returns200() {
        createRoom("R1");
        createSensor("S1", "temperature", "ACTIVE", "R1");
        Response r = target("/sensors/S1").request().get();
        assertEquals(200, r.getStatus());
    }

    @Test
    void getSensorById_notFound_returns404() {
        Response r = target("/sensors/GHOST").request().get();
        assertEquals(404, r.getStatus());
    }
}
