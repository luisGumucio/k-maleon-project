package com.kmaleon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseRequest {

    @NotNull(message = "itemId is required")
    private UUID itemId;

    @NotNull(message = "unitId is required")
    private UUID unitId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.000001", message = "quantity must be greater than zero")
    private BigDecimal quantity;

    private String notes;

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getUnitId() { return unitId; }
    public void setUnitId(UUID unitId) { this.unitId = unitId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
