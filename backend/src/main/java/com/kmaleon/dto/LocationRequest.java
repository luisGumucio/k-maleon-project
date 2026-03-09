package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LocationRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "type is required")
    @Pattern(regexp = "warehouse|branch", message = "type must be 'warehouse' or 'branch'")
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
