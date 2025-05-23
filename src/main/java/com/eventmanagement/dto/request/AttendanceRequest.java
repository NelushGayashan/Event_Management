package com.eventmanagement.dto.request;

import com.eventmanagement.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AttendanceRequest {

    @NotNull
    private UUID eventId;

    @NotNull
    private AttendanceStatus status;

    // Constructors
    public AttendanceRequest() {}

    public AttendanceRequest(UUID eventId, AttendanceStatus status) {
        this.eventId = eventId;
        this.status = status;
    }

    // Getters and Setters
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}