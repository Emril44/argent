package org.example.user;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {
    private final UUID id;
    private final String name;
    private final String email;
    private UserStatus status;
    private final LocalDateTime createdAt;

    public UserResponse(UUID id, String name, String email, UserStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
