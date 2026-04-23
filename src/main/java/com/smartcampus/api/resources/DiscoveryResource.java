package com.smartcampus.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovery endpoint - GET /api/v1/
 * Returns API metadata, version info, and resource links (HATEOAS).
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "REST API for managing campus rooms and IoT sensors.");
        response.put("contact", "admin@smartcampus.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("self", "/api/v1/");
        response.put("resources", links);

        return Response.ok(response).build();
    }
}
