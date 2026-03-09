package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ItemRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "baseUnitId is required")
    private UUID baseUnitId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getBaseUnitId() { return baseUnitId; }
    public void setBaseUnitId(UUID baseUnitId) { this.baseUnitId = baseUnitId; }
}
