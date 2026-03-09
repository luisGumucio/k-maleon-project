package com.kmaleon.security;

import java.util.UUID;

public class AuthenticatedUser {

    private final UUID id;
    private final String role;
    private final String name;

    public AuthenticatedUser(UUID id, String role, String name) {
        this.id = id;
        this.role = role;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getRole() { return role; }
    public String getName() { return name; }
}
