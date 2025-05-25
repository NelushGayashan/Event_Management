// src/test/java/com/eventmanagement/service/EventServiceTest.java
package com.eventmanagement.service;

import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.Role;
import com.eventmanagement.enums.Visibility;
import com.eventmanagement.exception.ResourceNotFoundException;
import com.eventmanagement.mapper.EventMapper;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private UserService userService;

    @Mock
    private FilterService filterService;

    @InjectMocks
    private EventServiceImpl eventService;

    private CreateEventRequest createRequest;
    private UpdateEventRequest updateRequest;
    private Event testEvent;
    private User testUser;
    private EventResponse eventResponse;
    private UUID userId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.USER);

        createRequest = new CreateEventRequest();
        createRequest.setTitle("Test Event");
        createRequest.setDescription("Test Description");
        createRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        createRequest.setLocation("Test Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Updated Event");
        updateRequest.setDescription("Updated Description");

        testEvent = new Event();
        testEvent.setId(eventId);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setHost(testUser);
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setVisibility(Visibility.PUBLIC);

        eventResponse = new EventResponse();
        eventResponse.setId(eventId);
        eventResponse.setTitle("Test Event");
        eventResponse.setDescription("Test Description");
        eventResponse.setHostId(userId);
        eventResponse.setHostName("John Doe");
    }

    @Test
    void whenCreateEvent_withValidRequest_thenReturnEventResponse() {
        when(userService.getEntityById(userId)).thenReturn(testUser);
        when(eventMapper.toEntity(createRequest, testUser)).thenReturn(testEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(testEvent)).thenReturn(eventResponse);

        EventResponse response = eventService.createEvent(createRequest, userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getTitle()).isEqualTo("Test Event");
        assertThat(response.getHostId()).isEqualTo(userId);
        assertThat(response.getHostName()).isEqualTo("John Doe");

        verify(userService).getEntityById(userId);
        verify(eventRepository).save(any(Event.class));
        verify(eventMapper).toResponse(testEvent);
        verify(filterService).enableSoftDeleteFilter();
    }

    @Test
    void whenUpdateEvent_withValidRequest_thenReturnUpdatedEventResponse() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(testEvent)).thenReturn(eventResponse);

        EventResponse response = eventService.updateEvent(eventId, updateRequest, userId);

        assertThat(response).isNotNull();
        verify(eventMapper).updateEntityFromRequest(updateRequest, testEvent);
        verify(eventRepository).save(testEvent);
        verify(filterService).enableSoftDeleteFilter();
    }

    @Test
    void whenUpdateEvent_withNonExistentEvent_thenThrowResourceNotFoundException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventId, updateRequest, userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
        verify(filterService).enableSoftDeleteFilter();
    }

    @Test
    void whenDeleteEvent_withValidEventAndUser_thenDeleteEvent() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        eventService.deleteEvent(eventId, userId);

        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(testEvent);
        verify(filterService).enableSoftDeleteFilter();
    }

    @Test
    void whenDeleteEvent_withNonExistentEvent_thenThrowResourceNotFoundException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(eventId, userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).delete(any(Event.class));
        verify(filterService).enableSoftDeleteFilter();
    }
}