package com.smartcampus.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for /api/v1/sensors/{id}/readings — GET and POST sub-resource.
 */
class SensorReadingResourceTest extends BaseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void createRoom(String id) {
        String json = String.format(
            "{\"id\":\"%s\",\"name\":\"Room %s\",\"capacity\":30,\"sensorIds\":[]}", id, id);
        target("/rooms").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    private void createSensor(String id, String status) {
        String json = String.format(
            "{\"id\":\"%s\",\"type\":\"temperature\",\"status\":\"%s\",\"currentValue\":0,\"roomId\":\"R1\"}",
            id, status);
        target("/sensors").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    private Response postReading(String sensorId, String readingId, long ts, double value) {
        String json = String.format(
            "{\"id\":\"%s\",\"timestamp\":%d,\"value\":%s}", readingId, ts, value);
        return target("/sensors/" + sensorId + "/readings").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    // ── POST readings ─────────────────────────────────────────────────────────

    @Test
    void addReading_activeSensor_returns201() {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        Response r = postReading("S1", "RD1", 1713780000L, 26.5);
        assertEquals(201, r.getStatus());
    }

    @Test
    void addReading_activeSensor_responseBodyContainsValue() {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        String body = postReading("S1", "RD1", 1713780000L, 26.5).readEntity(String.class);
        assertTrue(body.contains("26.5"));
    }

    @Test
    void addReading_maintenanceSensor_returns403() {
        createRoom("R1");
        createSensor("S1", "MAINTENANCE");
        Response r = postReading("S1", "RD1", 1713780000L, 30.0);
        assertEquals(403, r.getStatus());
    }

    @Test
    void addReading_maintenanceSensor_errorBodyMentionsMaintenance() {
        createRoom("R1");
        createSensor("S1", "MAINTENANCE");
        String body = postReading("S1", "RD1", 1713780000L, 30.0).readEntity(String.class);
        assertTrue(body.toLowerCase().contains("maintenance"));
    }

    @Test
    void addReading_unknownSensor_returns404() {
        Response r = postReading("GHOST", "RD1", 1713780000L, 10.0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void addReading_missingReadingId_returns400() {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        String json = "{\"timestamp\":1713780000,\"value\":26.5}";
        Response r = target("/sensors/S1/readings").request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
        assertEquals(400, r.getStatus());
    }

    @Test
    void addReading_updatesParentSensorCurrentValue() throws Exception {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        postReading("S1", "RD1", 1713780000L, 99.9);

        JsonNode sensor = MAPPER.readTree(target("/sensors/S1").request().get(String.class));
        assertEquals(99.9, sensor.get("currentValue").asDouble(), 0.001);
    }

    // ── GET readings ──────────────────────────────────────────────────────────

    @Test
    void getReadings_noReadingsYet_returnsEmptyArray() throws Exception {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        Response r = target("/sensors/S1/readings").request().get();
        assertEquals(200, r.getStatus());
        JsonNode body = MAPPER.readTree(r.readEntity(String.class));
        assertTrue(body.isArray());
        assertEquals(0, body.size());
    }

    @Test
    void getReadings_afterOnePost_returnsOneReading() throws Exception {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        postReading("S1", "RD1", 1713780000L, 26.5);

        JsonNode body = MAPPER.readTree(
            target("/sensors/S1/readings").request().get(String.class));
        assertEquals(1, body.size());
        assertEquals(26.5, body.get(0).get("value").asDouble(), 0.001);
    }

    @Test
    void getReadings_multipleReadings_returnsAll() throws Exception {
        createRoom("R1");
        createSensor("S1", "ACTIVE");
        postReading("S1", "RD1", 1713780000L, 20.0);
        postReading("S1", "RD2", 1713780001L, 21.0);
        postReading("S1", "RD3", 1713780002L, 22.0);

        JsonNode body = MAPPER.readTree(
            target("/sensors/S1/readings").request().get(String.class));
        assertEquals(3, body.size());
    }

    @Test
    void getReadings_unknownSensor_returns404() {
        Response r = target("/sensors/GHOST/readings").request().get();
        assertEquals(404, r.getStatus());
    }
}
