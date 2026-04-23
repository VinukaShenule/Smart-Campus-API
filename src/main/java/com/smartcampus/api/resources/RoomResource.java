package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ResourceNotFoundException;
import com.smartcampus.api.exceptions.RoomNotEmptyException;
import com.smartcampus.api.models.Room;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages /api/v1/rooms
 *
 * DELETE idempotency: The first DELETE on an existing room removes it (200).
 * Subsequent DELETE calls on the same room ID return 404 since it no longer exists.
 * This is technically NOT fully idempotent in terms of response code, but the server
 * state remains the same (room is gone) - which satisfies the REST idempotency contract
 * on state, while informing the client that the resource was already removed.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // GET /api/v1/rooms/{id}
    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = store.getRoomById(id);
        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + id + "' was not found.");
        }
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Room id is required.\"}")
                    .build();
        }
        if (store.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\":\"Room with id '" + room.getId() + "' already exists.\"}")
                    .build();
        }
        // Ensure sensorIds list is initialised
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        store.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // DELETE /api/v1/rooms/{id}
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = store.getRoomById(id);
        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + id + "' was not found.");
        }
        // Business rule: cannot delete a room that still has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(id);
        }
        store.deleteRoom(id);
        return Response.ok("{\"message\":\"Room '" + id + "' has been successfully deleted.\"}").build();
    }
}
