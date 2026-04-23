package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.api.exceptions.ResourceNotFoundException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages /api/v1/sensors
 *
 * Uses @Consumes(APPLICATION_JSON): if client sends text/plain or application/xml,
 * JAX-RS returns 415 Unsupported Media Type automatically - the method is never invoked.
 *
 * @QueryParam vs @PathParam for filtering:
 * Using ?type=CO2 is better than /sensors/type/CO2 because filtering/searching is not
 * identifying a specific resource - it's narrowing a collection. Query params are
 * optional by nature, composable (can add ?status=ACTIVE&type=CO2), and semantically
 * clearer. Path params imply a unique resource identity.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors  OR  GET /api/v1/sensors?type=temperature
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isBlank()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensorList) {
                if (type.equalsIgnoreCase(s.getType())) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }
        return Response.ok(sensorList).build();
    }

    // GET /api/v1/sensors/{id}
    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = store.getSensorById(id);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with id '" + id + "' was not found.");
        }
        return Response.ok(sensor).build();
    }

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Sensor id is required.\"}")
                    .build();
        }
        // 422: roomId must reference an existing room
        if (sensor.getRoomId() == null || !store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The roomId '" + sensor.getRoomId() + "' does not reference an existing room. " +
                    "Please create the room first before registering a sensor to it.");
        }
        if (store.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\":\"Sensor with id '" + sensor.getId() + "' already exists.\"}")
                    .build();
        }
        store.addSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /**
     * Sub-resource locator: delegates all /sensors/{sensorId}/readings paths
     * to SensorReadingResource. This pattern keeps concerns separated -
     * SensorResource handles sensor CRUD, SensorReadingResource handles readings.
     * In large APIs this prevents a single "god class" resource with hundreds of methods.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
