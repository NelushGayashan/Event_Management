// src/main/java/com/eventmanagement/controller/EventController.java
package com.eventmanagement.controller;

import com.eventmanagement.dto.request.AttendanceRequest;
import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventDetailResponse;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.dto.response.PagedResponse;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Visibility;
import com.eventmanagement.security.UserPrincipal;
import com.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EventResponse response = eventService.createEvent(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EventResponse response = eventService.updateEvent(id, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        eventService.deleteEvent(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailResponse> getEventDetails(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UUID userId = currentUser != null ? currentUser.getId() : null;
        EventDetailResponse response = eventService.getEventDetails(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> getAllEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) UUID hostId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<EventResponse> response = eventService.getAllEvents(
                title, location, startDate, endDate, visibility, hostId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<EventResponse>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        PagedResponse<EventResponse> response = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<EventResponse>> getUserEvents(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<EventResponse> response = eventService.getUserEvents(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attending")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<EventResponse>> getUserAttendingEvents(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        PagedResponse<EventResponse> response = eventService.getUserAttendingEvents(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updateAttendance(
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        eventService.updateAttendance(request, currentUser.getId());
        return ResponseEntity.ok("Attendance updated successfully");
    }

    @GetMapping("/{id}/attendance-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceStatus> getUserAttendanceStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceStatus status = eventService.getUserAttendanceStatus(id, currentUser.getId());
        return ResponseEntity.ok(status);
    }
}
