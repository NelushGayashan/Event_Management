// EventServiceTest.java
package com.eventmanagement.service;

import com.eventmanagement.dto.request.AttendanceRequest;
import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventDetailResponse;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.dto.response.PagedResponse;
import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Role;
import com.eventmanagement.enums.Visibility;
import com.eventmanagement.exception.BadRequestException;
import com.eventmanagement.exception.ResourceNotFoundException;
import com.eventmanagement.exception.UnauthorizedException;
import com.eventmanagement.mapper.EventMapper;
import com.eventmanagement.repository.AttendanceRepository;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User testUser;
    private Event testEvent;
    private CreateEventRequest createEventRequest;
    private UpdateEventRequest updateEventRequest;
    private EventResponse eventResponse;
    private EventDetailResponse eventDetailResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.USER);

        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setHost(testUser);
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setVisibility(Visibility.PUBLIC);

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
        eventResponse.setId(testEvent.getId());
        eventResponse.setTitle("Test Event");

        eventDetailResponse = new EventDetailResponse();
        eventDetailResponse.setId(testEvent.getId());
        eventDetailResponse.setTitle("Test Event");
    }

    @Test
    void whenCreateEvent_withValidRequest_thenReturnEventResponse() {
        when(userService.getEntityById(any(UUID.class))).thenReturn(testUser);
        when(eventMapper.toEntity(any(CreateEventRequest.class), any(User.class))).thenReturn(testEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(eventResponse);

        EventResponse response = eventService.createEvent(createEventRequest, testUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testEvent.getId());
        assertThat(response.getTitle()).isEqualTo("Test Event");

        verify(userService).getEntityById(testUser.getId());
        verify(eventRepository).save(any(Event.class));
        verify(eventMapper).toResponse(testEvent);
    }

    @Test
    void whenCreateEvent_withEndTimeBeforeStartTime_thenThrowBadRequestException() {
        createEventRequest.setEndTime(LocalDateTime.now().plusDays(1).minusHours(1));

        assertThatThrownBy(() -> eventService.createEvent(createEventRequest, testUser.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("End time must be after start time");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void whenUpdateEvent_asHost_thenReturnUpdatedEventResponse() {
        // Given
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getEntityById(any(UUID.class))).thenReturn(testUser);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(eventResponse);

        EventResponse response = eventService.updateEvent(testEvent.getId(), updateEventRequest, testUser.getId());

        assertThat(response).isNotNull();
        verify(eventMapper).updateEntityFromRequest(updateEventRequest, testEvent);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void whenUpdateEvent_asNonHostNonAdmin_thenThrowUnauthorizedException() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(Role.USER);

        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getEntityById(any(UUID.class))).thenReturn(otherUser);

        assertThatThrownBy(() -> eventService.updateEvent(testEvent.getId(), updateEventRequest, otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You can only update events you are hosting");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void whenUpdateEvent_withNonExistentEvent_thenThrowResourceNotFoundException() {
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(UUID.randomUUID(), updateEventRequest, testUser.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void whenDeleteEvent_asHost_thenSoftDeleteEvent() {
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getEntityById(any(UUID.class))).thenReturn(testUser);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        eventService.deleteEvent(testEvent.getId(), testUser.getId());

        verify(eventRepository).save(testEvent);
        assertThat(testEvent.getDeletedAt()).isNotNull();
    }

    @Test
    void whenGetEventDetails_withPublicEvent_thenReturnEventDetailResponse() {
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(eventMapper.toDetailResponse(any(Event.class))).thenReturn(eventDetailResponse);
        when(attendanceRepository.countAttendanceByStatus(any(UUID.class))).thenReturn(List.of());
        when(attendanceRepository.findByEventId(any(UUID.class))).thenReturn(List.of());

        EventDetailResponse response = eventService.getEventDetails(testEvent.getId(), testUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testEvent.getId());
        verify(eventRepository).findById(testEvent.getId());
        verify(eventMapper).toDetailResponse(testEvent);
    }

    @Test
    void whenGetEventDetails_withPrivateEventAsNonAuthorizedUser_thenThrowUnauthorizedException() {
        testEvent.setVisibility(Visibility.PRIVATE);
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(Role.USER);

        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getEntityById(any(UUID.class))).thenReturn(otherUser);
        when(attendanceRepository.existsByEventIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(false);

        assertThatThrownBy(() -> eventService.getEventDetails(testEvent.getId(), otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You don't have access to this private event");
    }

    @Test
    void whenGetAllEvents_thenReturnPagedResponse() {
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> eventPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        List<EventResponse> eventResponses = Arrays.asList(eventResponse);

        when(eventRepository.findEventsWithFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(eventPage);
        when(eventMapper.toResponseList(any())).thenReturn(eventResponses);

        PagedResponse<EventResponse> response = eventService.getAllEvents(
                null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    void whenUpdateAttendance_withValidRequest_thenUpdateAttendance() {
        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setEventId(testEvent.getId());
        attendanceRequest.setStatus(AttendanceStatus.GOING);

        Attendance attendance = new Attendance();
        attendance.setEvent(testEvent);
        attendance.setUser(testUser);
        attendance.setStatus(AttendanceStatus.MAYBE);

        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getEntityById(any(UUID.class))).thenReturn(testUser);
        when(attendanceRepository.findById(any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        eventService.updateAttendance(attendanceRequest, testUser.getId());

        verify(attendanceRepository).save(any(Attendance.class));
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.GOING);
    }

    @Test
    void whenGetUserAttendanceStatus_withExistingAttendance_thenReturnStatus() {
        Attendance attendance = new Attendance();
        attendance.setStatus(AttendanceStatus.GOING);

        when(attendanceRepository.findByEventIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(attendance));

        AttendanceStatus status = eventService.getUserAttendanceStatus(testEvent.getId(), testUser.getId());

        assertThat(status).isEqualTo(AttendanceStatus.GOING);
    }

    @Test
    void whenGetUserAttendanceStatus_withNoAttendance_thenReturnNone() {
        when(attendanceRepository.findByEventIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        AttendanceStatus status = eventService.getUserAttendanceStatus(testEvent.getId(), testUser.getId());

        assertThat(status).isEqualTo(AttendanceStatus.NONE);
    }
}
