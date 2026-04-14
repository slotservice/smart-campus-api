package com.smartcampus.resource;

import com.smartcampus.dto.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(DataStore.getSensors().values());

        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            ErrorResponse err = new ErrorResponse(400, "Bad Request", "Sensor ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            ErrorResponse err = new ErrorResponse(400, "Bad Request", "Room ID (roomId) is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        Room room = DataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Room with ID '" + sensor.getRoomId()
                    + "' does not exist. Cannot register a sensor to a non-existent room.");
        }

        if (DataStore.getSensor(sensor.getId()) != null) {
            ErrorResponse err = new ErrorResponse(409, "Conflict",
                    "Sensor with ID '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }

        DataStore.addSensor(sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
