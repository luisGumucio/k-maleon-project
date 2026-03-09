package com.kmaleon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequestDto {

    @NotNull(message = "El item es requerido")
    private UUID itemId;

    @NotNull(message = "La unidad es requerida")
    private UUID unitId;

    @NotNull(message = "La cantidad es requerida")
    @DecimalMin(value = "0.000001", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @NotNull(message = "La ubicación de destino es requerida")
    private UUID locationToId;

    private String notes;

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getUnitId() { return unitId; }
    public void setUnitId(UUID unitId) { this.unitId = unitId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public UUID getLocationToId() { return locationToId; }
    public void setLocationToId(UUID locationToId) { this.locationToId = locationToId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
