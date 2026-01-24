package com.example;

import java.time.LocalDateTime;

public class Booking {
    private final String id;
    private final String roomId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public Booking(String id, String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(endTime) && end.isAfter(startTime);
    }

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
