package org.example.user;

import java.time.LocalDateTime;

public class User {
    int id;
    String name;
    LocalDateTime createdAt;
    public User() {
        this.id = 1;
        this.name = "sbeebf";
        this.createdAt = LocalDateTime.now();
    }
}
