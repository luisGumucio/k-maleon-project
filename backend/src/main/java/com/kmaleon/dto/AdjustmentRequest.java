package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class AdjustmentRequest {

    @NotNull(message = "itemId is required")
    private UUID itemId;

    @NotNull(message = "locationId is required")
    private UUID locationId;

    @NotNull(message = "unitId is required")
    private UUID unitId;

    @NotNull(message = "quantity is required")
    private BigDecimal quantity;

    @NotBlank(message = "notes are required for adjustments")
    private String notes;

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public UUID getUnitId() { return unitId; }
    public void setUnitId(UUID unitId) { this.unitId = unitId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
