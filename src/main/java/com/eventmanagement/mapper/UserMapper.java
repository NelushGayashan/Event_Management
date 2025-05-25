// src/main/java/com/eventmanagement/mapper/UserMapper.java
package com.eventmanagement.mapper;

import com.eventmanagement.dto.request.RegisterRequest;
import com.eventmanagement.dto.response.UserResponse;
import com.eventmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "hostedEvents", ignore = true)
    @Mapping(target = "attendances", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}