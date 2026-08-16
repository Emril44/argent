package org.example.user;

public class UserRegisteredEvent {
    private final String username;
    private final String email;
    private final UserStatus status;

    public UserRegisteredEvent(String username, String email, UserStatus status) {
        this.username = username;
        this.email = email;
        this.status = status;
    }
}
