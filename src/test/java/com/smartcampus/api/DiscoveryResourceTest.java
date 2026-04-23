package com.smartcampus.api;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GET /api/v1/ — Discovery endpoint.
 */
class DiscoveryResourceTest extends BaseTest {

    @Test
    void discovery_returns200() {
        Response r = target("/").request().get();
        assertEquals(200, r.getStatus());
    }

    @Test
    void discovery_returnsJsonContentType() {
        Response r = target("/").request().get();
        assertTrue(r.getMediaType().toString().contains("application/json"));
    }

    @Test
    void discovery_bodyContainsVersionField() {
        String body = target("/").request().get(String.class);
        assertTrue(body.contains("version"), "Response should include 'version' field");
    }

    @Test
    void discovery_bodyContainsResourceLinks() {
        String body = target("/").request().get(String.class);
        assertTrue(body.contains("rooms"), "Response should include 'rooms' link");
        assertTrue(body.contains("sensors"), "Response should include 'sensors' link");
    }
}
