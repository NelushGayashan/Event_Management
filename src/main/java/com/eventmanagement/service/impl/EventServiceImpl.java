package com.eventmanagement.service.impl;

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
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EventMapper eventMapper;

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public EventResponse createEvent(CreateEventRequest request, UUID userId) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }

        Event event = eventMapper.toEntity(request, host);
        event = eventRepository.save(event);

        return eventMapper.toResponse(event);
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!event.getHost().getId().equals(userId) && !user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("You can only update events you are hosting");
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }
        }

        eventMapper.updateEntityFromRequest(request, event);
        event = eventRepository.save(event);

        return eventMapper.toResponse(event);
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public void deleteEvent(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!event.getHost().getId().equals(userId) && !user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("You can only delete events you are hosting");
        }

        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    @Override
    @Cacheable(value = "events", key = "#eventId")
    public EventDetailResponse getEventDetails(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getVisibility() == Visibility.PRIVATE) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            if (!event.getHost().getId().equals(userId) && !user.getRole().equals(Role.ADMIN)) {
                throw new UnauthorizedException("You don't have permission to view this private event");
            }
        }

        EventDetailResponse response = eventMapper.toDetailResponse(event);

        List<Object[]> attendanceStats = attendanceRepository.countAttendanceByStatus(eventId);
        Map<AttendanceStatus, Long> attendanceBreakdown = attendanceStats.stream()
                .collect(Collectors.toMap(
                        stat -> (AttendanceStatus) stat[0],
                        stat -> (Long) stat[1]
                ));

        response.setAttendanceBreakdown(attendanceBreakdown);
        response.setAttendeeCount(
                attendanceBreakdown.values().stream().mapToLong(Long::longValue).sum()
        );

        List<Attendance> attendances = attendanceRepository.findByEventId(eventId);
        List<EventDetailResponse.AttendeeResponse> attendees = attendances.stream()
                .map(attendance -> new EventDetailResponse.AttendeeResponse(
                        attendance.getUser().getId(),
                        attendance.getUser().getName(),
                        attendance.getStatus(),
                        attendance.getRespondedAt()
                ))
                .collect(Collectors.toList());

        response.setAttendees(attendees);

        return response;
    }

    @Override
    @Cacheable(value = "events")
    public PagedResponse<EventResponse> getAllEvents(String title, String location,
                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                     Visibility visibility, UUID hostId,
                                                     Pageable pageable) {
        Page<Event> eventPage = eventRepository.findEventsWithFilters(
                title, location, startDate, endDate, visibility, hostId, pageable);

        List<EventResponse> eventResponses = eventMapper.toResponseList(eventPage.getContent());

        return new PagedResponse<>(
                eventResponses,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Override
    @Cacheable(value = "upcomingEvents")
    public PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findUpcomingPublicEvents(LocalDateTime.now(), pageable);

        List<EventResponse> eventResponses = eventMapper.toResponseList(eventPage.getContent());

        return new PagedResponse<>(
                eventResponses,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Override
    public PagedResponse<EventResponse> getUserEvents(UUID userId, Pageable pageable) {
        Page<Event> eventPage = eventRepository.findByHostId(userId, pageable);

        List<EventResponse> eventResponses = eventMapper.toResponseList(eventPage.getContent());

        return new PagedResponse<>(
                eventResponses,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Override
    public PagedResponse<EventResponse> getUserAttendingEvents(UUID userId, Pageable pageable) {
        Page<Event> eventPage = eventRepository.findEventsByAttendeeId(userId, pageable);

        List<EventResponse> eventResponses = eventMapper.toResponseList(eventPage.getContent());

        return new PagedResponse<>(
                eventResponses,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public void updateAttendance(AttendanceRequest request, UUID userId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Attendance attendance = attendanceRepository
                .findByEventIdAndUserId(request.getEventId(), userId)
                .orElse(new Attendance());

        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(request.getStatus());
        attendance.setRespondedAt(LocalDateTime.now());

        attendanceRepository.save(attendance);
    }

    @Override
    public AttendanceStatus getUserAttendanceStatus(UUID eventId, UUID userId) {
        return attendanceRepository.findByEventIdAndUserId(eventId, userId)
                .map(Attendance::getStatus)
                .orElse(AttendanceStatus.NONE);
    }
}
