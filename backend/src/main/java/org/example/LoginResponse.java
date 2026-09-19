package org.example;

import java.util.UUID;

public class LoginResponse {
    private final UUID userId;
    private final String username;

    public LoginResponse(UUID userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
