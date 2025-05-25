// src/test/java/com/eventmanagement/service/UserServiceTest.java
package com.eventmanagement.service;

import com.eventmanagement.dto.response.UserResponse;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.Role;
import com.eventmanagement.exception.ResourceNotFoundException;
import com.eventmanagement.mapper.UserMapper;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.USER);

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName("John Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setRole(Role.USER);
    }

    @Test
    void whenGetUserById_withValidId_thenReturnUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void whenGetUserById_withInvalidId_thenThrowResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void whenGetAllUsers_thenReturnUserResponseList() {
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(Role.ADMIN);

        UserResponse userResponse2 = new UserResponse();
        userResponse2.setId(user2.getId());
        userResponse2.setName("Jane Smith");
        userResponse2.setEmail("jane.smith@example.com");
        userResponse2.setRole(Role.ADMIN);

        List<User> users = Arrays.asList(testUser, user2);
        List<UserResponse> userResponses = Arrays.asList(userResponse, userResponse2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        when(userMapper.toResponse(user2)).thenReturn(userResponse2);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");

        verify(userRepository).findAll();
        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    @Test
    void whenDeactivateUser_withValidId_thenSetDeletedAt() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(userId);

        assertThat(testUser.getDeletedAt()).isNotNull();
        assertThat(testUser.getDeletedAt()).isBefore(LocalDateTime.now().plusSeconds(1));

        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void whenDeactivateUser_withInvalidId_thenThrowResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenGetEntityById_withValidId_thenReturnUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.getEntityById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John Doe");

        verify(userRepository).findById(userId);
    }

    @Test
    void whenGetEntityById_withInvalidId_thenThrowResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getEntityById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");

        verify(userRepository).findById(userId);
    }
}
