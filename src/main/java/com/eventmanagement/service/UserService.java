package com.eventmanagement.service;

import com.eventmanagement.dto.response.UserResponse;
import com.eventmanagement.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getUserById(UUID userId);
    List<UserResponse> getAllUsers();
    void deactivateUser(UUID userId);
    User getEntityById(UUID userId);
}
