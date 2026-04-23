package com.smartcampus.api.exceptions;

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently in MAINTENANCE mode and cannot accept new readings.");
    }
}
