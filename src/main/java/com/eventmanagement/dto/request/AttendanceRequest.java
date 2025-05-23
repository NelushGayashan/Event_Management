// src/main/java/com/eventmanagement/dto/request/AttendanceRequest.java
package com.eventmanagement.dto.request;

import com.eventmanagement.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AttendanceRequest {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    public AttendanceRequest() {}

    public AttendanceRequest(UUID eventId, AttendanceStatus status) {
        this.eventId = eventId;
        this.status = status;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}