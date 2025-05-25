// src/test/java/com/eventmanagement/controller/EventControllerTest.java
package com.eventmanagement.controller;

import com.eventmanagement.config.BaseWebMvcTest;
import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.dto.response.PagedResponse;
import com.eventmanagement.enums.Visibility;
import com.eventmanagement.security.UserPrincipal;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.FilterService;
import com.eventmanagement.service.SoftDeleteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest extends BaseWebMvcTest {

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private SoftDeleteService softDeleteService;

    @MockBean
    private FilterService filterService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateEventRequest createRequest;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateEventRequest();
        createRequest.setTitle("Test Event");
        createRequest.setDescription("Test Description");
        createRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createRequest.setLocation("Test Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        eventResponse = new EventResponse();
        eventResponse.setId(UUID.randomUUID());
        eventResponse.setTitle("Test Event");
        eventResponse.setDescription("Test Description");
        eventResponse.setLocation("Test Location");
        eventResponse.setVisibility(Visibility.PUBLIC);
        eventResponse.setStartTime(createRequest.getStartTime());
        eventResponse.setEndTime(createRequest.getEndTime());
    }

    @Test
    void whenCreateEvent_withValidRequest_thenReturnEventResponse() throws Exception {
        UUID userId = UUID.randomUUID();

        UserPrincipal mockUserPrincipal = new UserPrincipal(
                userId,
                "Test User",
                "testuser@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(eventService.createEvent(any(CreateEventRequest.class), eq(userId)))
                .thenReturn(eventResponse);

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUserPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.location").value("Test Location"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        verify(eventService).createEvent(any(CreateEventRequest.class), eq(userId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetUpcomingEvents_thenReturnPagedEventsList() throws Exception {
        var events = List.of(eventResponse);
        var pagedResponse = new PagedResponse<>(events, 0, 10, 1L, 1);

        when(eventService.getUpcomingEvents(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Event"))
                .andExpect(jsonPath("$.content[0].location").value("Test Location"));

        verify(eventService).getUpcomingEvents(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetAllEvents_withFilters_thenReturnFilteredPagedEvents() throws Exception {
        var filteredEvents = List.of(eventResponse);
        var pagedResponse = new PagedResponse<>(filteredEvents, 0, 10, 1L, 1);

        when(eventService.getAllEvents(
                eq("Test"), eq("Test Location"), any(), any(), eq(Visibility.PUBLIC), any(), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/events")
                        .param("title", "Test")
                        .param("location", "Test Location")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Event"))
                .andExpect(jsonPath("$.content[0].location").value("Test Location"));

        verify(eventService).getAllEvents(
                eq("Test"), eq("Test Location"), any(), any(), eq(Visibility.PUBLIC), any(), any());
    }
}
