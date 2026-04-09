package com.smartcampus.resource;

import com.smartcampus.dto.ErrorResponse;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms — return all rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.getRooms().values())).build();
    }

    // POST /api/v1/rooms — create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            ErrorResponse err = new ErrorResponse(400, "Bad Request", "Room ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (DataStore.getRoom(room.getId()) != null) {
            ErrorResponse err = new ErrorResponse(409, "Conflict",
                    "Room with ID '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        DataStore.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — get a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            ErrorResponse err = new ErrorResponse(404, "Not Found",
                    "Room with ID '" + roomId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — delete a room (only if it has no sensors)
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            ErrorResponse err = new ErrorResponse(404, "Not Found",
                    "Room with ID '" + roomId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        // Business rule: cannot delete a room that still has sensors assigned
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "'. It still has "
                    + room.getSensorIds().size()
                    + " sensor(s) assigned to it. Remove all sensors first.");
        }
        DataStore.removeRoom(roomId);
        return Response.noContent().build();
    }
}
