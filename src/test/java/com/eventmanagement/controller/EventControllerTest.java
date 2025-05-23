// EventControllerTest.java
package com.eventmanagement.controller;

import com.eventmanagement.dto.request.AttendanceRequest;
import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventDetailResponse;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.dto.response.PagedResponse;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Visibility;
import com.eventmanagement.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateEventRequest createEventRequest;
    private UpdateEventRequest updateEventRequest;
    private EventResponse eventResponse;
    private EventDetailResponse eventDetailResponse;
    private PagedResponse<EventResponse> pagedResponse;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        createEventRequest = new CreateEventRequest();
        createEventRequest.setTitle("Test Event");
        createEventRequest.setDescription("Test Description");
        createEventRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createEventRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createEventRequest.setLocation("Test Location");
        createEventRequest.setVisibility(Visibility.PUBLIC);

        updateEventRequest = new UpdateEventRequest();
        updateEventRequest.setTitle("Updated Event");

        eventResponse = new EventResponse();
        eventResponse.setId(eventId);
        eventResponse.setTitle("Test Event");

        eventDetailResponse = new EventDetailResponse();
        eventDetailResponse.setId(eventId);
        eventDetailResponse.setTitle("Test Event");

        pagedResponse = new PagedResponse<>(
                Collections.singletonList(eventResponse), 0, 10, 1, 1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenCreateEvent_withValidRequest_thenReturnCreatedEvent() throws Exception {
        when(eventService.createEvent(any(CreateEventRequest.class), any(UUID.class)))
                .thenReturn(eventResponse);

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).createEvent(any(CreateEventRequest.class), any(UUID.class));
    }

    @Test
    void whenCreateEvent_withoutAuthentication_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenUpdateEvent_withValidRequest_thenReturnUpdatedEvent() throws Exception {
        when(eventService.updateEvent(any(UUID.class), any(UpdateEventRequest.class), any(UUID.class)))
                .thenReturn(eventResponse);

        mockMvc.perform(put("/api/events/{id}", eventId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).updateEvent(eq(eventId), any(UpdateEventRequest.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenDeleteEvent_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", eventId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(eq(eventId), any(UUID.class));
    }

    @Test
    void whenGetEventDetails_thenReturnEventDetails() throws Exception {
        when(eventService.getEventDetails(any(UUID.class), any(UUID.class)))
                .thenReturn(eventDetailResponse);

        mockMvc.perform(get("/api/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).getEventDetails(eq(eventId), any());
    }

    @Test
    void whenGetAllEvents_thenReturnPagedEvents() throws Exception {
        when(eventService.getAllEvents(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(eventService).getAllEvents(any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void whenGetUpcomingEvents_thenReturnPagedEvents() throws Exception {
        when(eventService.getUpcomingEvents(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/events/upcoming")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(eventService).getUpcomingEvents(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetUserEvents_thenReturnUserEvents() throws Exception {
        when(eventService.getUserEvents(any(UUID.class), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/events/my-events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        verify(eventService).getUserEvents(any(UUID.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenUpdateAttendance_withValidRequest_thenReturnSuccessMessage() throws Exception {
        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setEventId(eventId);
        attendanceRequest.setStatus(AttendanceStatus.GOING);

        mockMvc.perform(post("/api/events/attendance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendanceRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Attendance updated successfully"));

        verify(eventService).updateAttendance(any(AttendanceRequest.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetUserAttendanceStatus_thenReturnAttendanceStatus() throws Exception {
        when(eventService.getUserAttendanceStatus(any(UUID.class), any(UUID.class)))
                .thenReturn(AttendanceStatus.GOING);

        mockMvc.perform(get("/api/events/{id}/attendance-status", eventId))
                .andExpect(status().isOk())
                .andExpect(content().string("\"GOING\""));

        verify(eventService).getUserAttendanceStatus(eq(eventId), any(UUID.class));
    }
}
