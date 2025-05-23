package com.eventmanagement.mapper;

import com.eventmanagement.dto.request.CreateEventRequest;
import com.eventmanagement.dto.request.UpdateEventRequest;
import com.eventmanagement.dto.response.EventDetailResponse;
import com.eventmanagement.dto.response.EventResponse;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.AttendanceStatus;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", source = "host")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "attendances", ignore = true)
    Event toEntity(CreateEventRequest request, User host);

    @Mapping(target = "hostId", source = "host.id")
    @Mapping(target = "hostName", source = "host.name")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    @Mapping(target = "hostId", source = "host.id")
    @Mapping(target = "hostName", source = "host.name")
    @Mapping(target = "attendeeCount", ignore = true)
    @Mapping(target = "attendanceBreakdown", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    EventDetailResponse toDetailResponse(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateEventRequest request, @MappingTarget Event event);
}