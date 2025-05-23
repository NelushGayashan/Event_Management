// src/main/java/com/eventmanagement/dto/response/AuthResponse.java
package com.eventmanagement.dto.response;

import com.eventmanagement.enums.Role;

import java.util.UUID;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String name;
    private String email;
    private Role role;

    public AuthResponse(String accessToken, UUID userId, String name, String email, Role role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}