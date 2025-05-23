// src/main/java/com/eventmanagement/dto/request/UpdateEventRequest.java
package com.eventmanagement.dto.request;

import com.eventmanagement.enums.Visibility;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateEventRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Size(min = 3, max = 500, message = "Location must be between 3 and 500 characters")
    private String location;

    private Visibility visibility;

    // Constructors
    public UpdateEventRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}