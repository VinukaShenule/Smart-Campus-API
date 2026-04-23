package com.smartcampus.api.exceptions;

public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " cannot be deleted because it still has active sensors assigned to it.");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
