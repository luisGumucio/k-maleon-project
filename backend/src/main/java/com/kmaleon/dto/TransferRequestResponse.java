package com.kmaleon.dto;

import com.kmaleon.model.TransferRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TransferRequestResponse {

    private UUID id;
    private UUID itemId;
    private String itemName;
    private UUID unitId;
    private String unitSymbol;
    private BigDecimal quantity;
    private BigDecimal quantityBase;
    private String baseUnitSymbol;
    private UUID locationId;
    private String locationName;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private TransferRequestResponse() {}

    public static TransferRequestResponse from(TransferRequest r) {
        return new Builder()
                .id(r.getId())
                .itemId(r.getItem().getId())
                .itemName(r.getItem().getName())
                .unitId(r.getUnit().getId())
                .unitSymbol(r.getUnit().getSymbol())
                .quantity(r.getQuantity())
                .quantityBase(r.getQuantityBase())
                .baseUnitSymbol(r.getItem().getBaseUnit().getSymbol())
                .locationId(r.getLocation().getId())
                .locationName(r.getLocation().getName())
                .status(r.getStatus())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    public static class Builder {
        private final TransferRequestResponse response = new TransferRequestResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder itemId(UUID itemId) { response.itemId = itemId; return this; }
        public Builder itemName(String itemName) { response.itemName = itemName; return this; }
        public Builder unitId(UUID unitId) { response.unitId = unitId; return this; }
        public Builder unitSymbol(String unitSymbol) { response.unitSymbol = unitSymbol; return this; }
        public Builder quantity(BigDecimal quantity) { response.quantity = quantity; return this; }
        public Builder quantityBase(BigDecimal quantityBase) { response.quantityBase = quantityBase; return this; }
        public Builder baseUnitSymbol(String baseUnitSymbol) { response.baseUnitSymbol = baseUnitSymbol; return this; }
        public Builder locationId(UUID locationId) { response.locationId = locationId; return this; }
        public Builder locationName(String locationName) { response.locationName = locationName; return this; }
        public Builder status(String status) { response.status = status; return this; }
        public Builder notes(String notes) { response.notes = notes; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { response.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { response.updatedAt = updatedAt; return this; }

        public TransferRequestResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public UUID getUnitId() { return unitId; }
    public String getUnitSymbol() { return unitSymbol; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getQuantityBase() { return quantityBase; }
    public String getBaseUnitSymbol() { return baseUnitSymbol; }
    public UUID getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
