// src/test/java/com/eventmanagement/controller/UserControllerTest.java
package com.eventmanagement.controller;

import com.eventmanagement.config.BaseWebMvcTest;
import com.eventmanagement.config.TestSecurityConfig;
import com.eventmanagement.dto.response.UserResponse;
import com.eventmanagement.enums.Role;
import com.eventmanagement.security.UserPrincipal;
import com.eventmanagement.service.FilterService;
import com.eventmanagement.service.SoftDeleteService;
import com.eventmanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest extends BaseWebMvcTest {

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SoftDeleteService softDeleteService;

    @MockBean
    private FilterService filterService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName("John Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setRole(Role.USER);
        userResponse.setCreatedAt(LocalDateTime.now().minusDays(30));
        userResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void whenGetCurrentUser_thenReturnUserResponse() throws Exception {
        UserPrincipal mockUserPrincipal = new UserPrincipal(
                userId, "John Doe", "john.doe@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userService.getUserById(any(UUID.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me")
                        .with(user(mockUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUserById(any(UUID.class));
    }

    @Test
    void whenGetCurrentUser_withoutAuthentication_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenGetAllUsers_asAdmin_thenReturnUsersList() throws Exception {
        UserResponse adminUser = new UserResponse();
        adminUser.setId(UUID.randomUUID());
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        List<UserResponse> users = Arrays.asList(userResponse, adminUser);
        when(userService.getAllUsers()).thenReturn(users);

        UserPrincipal adminPrincipal = new UserPrincipal(
                adminUser.getId(), "Admin User", "admin@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockMvc.perform(get("/api/users")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Admin User"));

        verify(userService).getAllUsers();
    }

    @Test
    void whenGetAllUsers_asUser_thenReturnForbidden() throws Exception {
        UserPrincipal userPrincipal = new UserPrincipal(
                userId, "John Doe", "john.doe@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        mockMvc.perform(get("/api/users")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenGetAllUsers_withoutAuthentication_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenGetUserById_asAdmin_thenReturnUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        UserPrincipal adminPrincipal = new UserPrincipal(
                UUID.randomUUID(), "Admin User", "admin@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUserById(userId);
    }

    @Test
    void whenGetUserById_asUser_thenReturnForbidden() throws Exception {
        UserPrincipal userPrincipal = new UserPrincipal(
                userId, "John Doe", "john.doe@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeactivateUser_asAdmin_thenReturnNoContent() throws Exception {
        UserPrincipal adminPrincipal = new UserPrincipal(
                UUID.randomUUID(), "Admin User", "admin@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUser(userId);
    }

    @Test
    void whenDeactivateUser_asUser_thenReturnForbidden() throws Exception {
        UserPrincipal userPrincipal = new UserPrincipal(
                userId, "John Doe", "john.doe@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeactivateUser_withoutAuthentication_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenDeactivateUser_withInvalidId_thenReturnBadRequest() throws Exception {
        String invalidId = "invalid-uuid";
        UserPrincipal adminPrincipal = new UserPrincipal(
                UUID.randomUUID(), "Admin User", "admin@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        mockMvc.perform(delete("/api/users/{id}", invalidId)
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isBadRequest());
    }
}
