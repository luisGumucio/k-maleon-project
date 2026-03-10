package com.kmaleon.security;

import java.util.UUID;

public class AuthenticatedUser {

    private final UUID id;
    private final String role;
    private final String name;
    private final UUID locationId;

    public AuthenticatedUser(UUID id, String role, String name, UUID locationId) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.locationId = locationId;
    }

    public UUID getId() { return id; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public UUID getLocationId() { return locationId; }
}
