package com.kmaleon.dto;

import com.kmaleon.model.UserProfile;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String role;
    private UUID locationId;
    private OffsetDateTime createdAt;

    public static UserResponse from(UserProfile profile) {
        UserResponse r = new UserResponse();
        r.id = profile.getId();
        r.name = profile.getName();
        r.role = profile.getRole();
        r.locationId = profile.getLocationId();
        r.createdAt = profile.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public UUID getLocationId() { return locationId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
