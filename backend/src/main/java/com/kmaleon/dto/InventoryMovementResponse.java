package com.kmaleon.dto;

import com.kmaleon.model.InventoryMovement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class InventoryMovementResponse {

    private UUID id;
    private UUID itemId;
    private String itemName;
    private UUID unitId;
    private String unitSymbol;
    private BigDecimal quantity;
    private BigDecimal quantityBase;
    private String baseUnitSymbol;
    private String movementType;
    private UUID locationFromId;
    private String locationFromName;
    private UUID locationToId;
    private String locationToName;
    private String notes;
    private OffsetDateTime createdAt;

    private InventoryMovementResponse() {}

    public static InventoryMovementResponse from(InventoryMovement m) {
        return new Builder()
                .id(m.getId())
                .itemId(m.getItem().getId())
                .itemName(m.getItem().getName())
                .unitId(m.getUnit().getId())
                .unitSymbol(m.getUnit().getSymbol())
                .quantity(m.getQuantity())
                .quantityBase(m.getQuantityBase())
                .baseUnitSymbol(m.getItem().getBaseUnit().getSymbol())
                .movementType(m.getMovementType())
                .locationFromId(m.getLocationFrom() != null ? m.getLocationFrom().getId() : null)
                .locationFromName(m.getLocationFrom() != null ? m.getLocationFrom().getName() : null)
                .locationToId(m.getLocationTo() != null ? m.getLocationTo().getId() : null)
                .locationToName(m.getLocationTo() != null ? m.getLocationTo().getName() : null)
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public static class Builder {
        private final InventoryMovementResponse r = new InventoryMovementResponse();

        public Builder id(UUID id) { r.id = id; return this; }
        public Builder itemId(UUID itemId) { r.itemId = itemId; return this; }
        public Builder itemName(String itemName) { r.itemName = itemName; return this; }
        public Builder unitId(UUID unitId) { r.unitId = unitId; return this; }
        public Builder unitSymbol(String unitSymbol) { r.unitSymbol = unitSymbol; return this; }
        public Builder quantity(BigDecimal quantity) { r.quantity = quantity; return this; }
        public Builder quantityBase(BigDecimal quantityBase) { r.quantityBase = quantityBase; return this; }
        public Builder baseUnitSymbol(String baseUnitSymbol) { r.baseUnitSymbol = baseUnitSymbol; return this; }
        public Builder movementType(String movementType) { r.movementType = movementType; return this; }
        public Builder locationFromId(UUID locationFromId) { r.locationFromId = locationFromId; return this; }
        public Builder locationFromName(String locationFromName) { r.locationFromName = locationFromName; return this; }
        public Builder locationToId(UUID locationToId) { r.locationToId = locationToId; return this; }
        public Builder locationToName(String locationToName) { r.locationToName = locationToName; return this; }
        public Builder notes(String notes) { r.notes = notes; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { r.createdAt = createdAt; return this; }

        public InventoryMovementResponse build() { return r; }
    }

    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public UUID getUnitId() { return unitId; }
    public String getUnitSymbol() { return unitSymbol; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getQuantityBase() { return quantityBase; }
    public String getBaseUnitSymbol() { return baseUnitSymbol; }
    public String getMovementType() { return movementType; }
    public UUID getLocationFromId() { return locationFromId; }
    public String getLocationFromName() { return locationFromName; }
    public UUID getLocationToId() { return locationToId; }
    public String getLocationToName() { return locationToName; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
