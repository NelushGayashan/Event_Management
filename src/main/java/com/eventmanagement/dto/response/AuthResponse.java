package com.eventmanagement.dto.response;

import com.eventmanagement.enums.Role;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String name;
    private String email;
    private Role role;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, UUID userId, String name, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}