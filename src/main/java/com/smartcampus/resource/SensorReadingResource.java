package com.smartcampus.resource;

import com.smartcampus.dto.ErrorResponse;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource for managing sensor readings.
 * NOT a root resource — instantiated by SensorResource's sub-resource locator.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings — get reading history
    @GET
    public Response getReadings() {
        List<SensorReading> readings = DataStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings — post a new reading
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            ErrorResponse err = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        // Business rule: sensor in MAINTENANCE mode cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId
                    + "' is currently in MAINTENANCE mode and cannot accept new readings.");
        }

        // Auto-generate reading ID (UUID) and timestamp if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading
        DataStore.addReading(sensorId, reading);

        // Side effect: update the parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
