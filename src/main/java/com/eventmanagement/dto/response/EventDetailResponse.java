// src/main/java/com/eventmanagement/dto/response/EventDetailResponse.java
package com.eventmanagement.dto.response;

import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Visibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private UUID hostId;
    private String hostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long attendeeCount;
    private Map<AttendanceStatus, Long> attendanceBreakdown;
    private List<AttendeeResponse> attendees;

    public EventDetailResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getHostId() { return hostId; }
    public void setHostId(UUID hostId) { this.hostId = hostId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getAttendeeCount() { return attendeeCount; }
    public void setAttendeeCount(Long attendeeCount) { this.attendeeCount = attendeeCount; }

    public Map<AttendanceStatus, Long> getAttendanceBreakdown() { return attendanceBreakdown; }
    public void setAttendanceBreakdown(Map<AttendanceStatus, Long> attendanceBreakdown) {
        this.attendanceBreakdown = attendanceBreakdown;
    }

    public List<AttendeeResponse> getAttendees() { return attendees; }
    public void setAttendees(List<AttendeeResponse> attendees) { this.attendees = attendees; }

    public static class AttendeeResponse {
        private UUID userId;
        private String userName;
        private AttendanceStatus status;
        private LocalDateTime respondedAt;

        public AttendeeResponse() {}

        public AttendeeResponse(UUID userId, String userName, AttendanceStatus status, LocalDateTime respondedAt) {
            this.userId = userId;
            this.userName = userName;
            this.status = status;
            this.respondedAt = respondedAt;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }

        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    }
}