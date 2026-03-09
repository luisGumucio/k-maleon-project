package com.kmaleon.dto;

import com.kmaleon.model.Location;

import java.util.UUID;

public class LocationResponse {

    private UUID id;
    private String name;
    private String type;
    private boolean active;

    private LocationResponse() {}

    public static LocationResponse from(Location location) {
        return new Builder()
                .id(location.getId())
                .name(location.getName())
                .type(location.getType())
                .active(location.isActive())
                .build();
    }

    public static class Builder {
        private final LocationResponse response = new LocationResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder type(String type) { response.type = type; return this; }
        public Builder active(boolean active) { response.active = active; return this; }

        public LocationResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
}
