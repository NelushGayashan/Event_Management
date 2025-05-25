// src/main/java/com/eventmanagement/service/EventService.java
package com.eventmanagement.service;

import com.eventmanagement.dto.request.AttendanceRequest;
import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventDetailResponse;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.dto.response.PagedResponse;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Visibility;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request, UUID userId);

    EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID userId);

    void deleteEvent(UUID eventId, UUID userId);

    EventDetailResponse getEventDetails(UUID eventId, UUID userId);

    PagedResponse<EventResponse> getAllEvents(String title, String location,
                                              LocalDateTime startDate, LocalDateTime endDate,
                                              Visibility visibility, UUID hostId, Pageable pageable);

    PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable);

    PagedResponse<EventResponse> getUserEvents(UUID userId, Pageable pageable);

    PagedResponse<EventResponse> getUserAttendingEvents(UUID userId, Pageable pageable);

    void updateAttendance(AttendanceRequest request, UUID userId);

    AttendanceStatus getUserAttendanceStatus(UUID eventId, UUID userId);
}