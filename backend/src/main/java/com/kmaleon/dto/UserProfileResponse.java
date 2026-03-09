package com.kmaleon.dto;

import com.kmaleon.model.UserProfile;

import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String name;
    private String role;

    public static UserProfileResponse from(UserProfile profile) {
        UserProfileResponse r = new UserProfileResponse();
        r.id = profile.getId();
        r.name = profile.getName();
        r.role = profile.getRole();
        return r;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
