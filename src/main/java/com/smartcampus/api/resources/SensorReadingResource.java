package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ResourceNotFoundException;
import com.smartcampus.api.exceptions.SensorUnavailableException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Sub-resource for sensor readings.
 * Accessed via SensorResource's sub-resource locator.
 * Handles: GET /api/v1/sensors/{sensorId}/readings
 *          POST /api/v1/sensors/{sensorId}/readings
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with id '" + sensorId + "' was not found.");
        }
        List<SensorReading> readingList = store.getReadingsForSensor(sensorId);
        return Response.ok(readingList).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with id '" + sensorId + "' was not found.");
        }
        // 403: sensor in maintenance cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }
        if (reading == null || reading.getId() == null || reading.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Reading id is required.\"}")
                    .build();
        }
        store.addReading(sensorId, reading);

        // Side effect: update sensor's currentValue to match the latest reading
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
