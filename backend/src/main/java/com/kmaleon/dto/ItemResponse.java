package com.kmaleon.dto;

import com.kmaleon.model.Item;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ItemResponse {

    private UUID id;
    private String name;
    private UUID baseUnitId;
    private String baseUnitName;
    private String baseUnitSymbol;
    private boolean active;
    private OffsetDateTime createdAt;

    private ItemResponse() {}

    public static ItemResponse from(Item item) {
        return new Builder()
                .id(item.getId())
                .name(item.getName())
                .baseUnitId(item.getBaseUnit().getId())
                .baseUnitName(item.getBaseUnit().getName())
                .baseUnitSymbol(item.getBaseUnit().getSymbol())
                .active(item.isActive())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static class Builder {
        private final ItemResponse response = new ItemResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder baseUnitId(UUID baseUnitId) { response.baseUnitId = baseUnitId; return this; }
        public Builder baseUnitName(String baseUnitName) { response.baseUnitName = baseUnitName; return this; }
        public Builder baseUnitSymbol(String baseUnitSymbol) { response.baseUnitSymbol = baseUnitSymbol; return this; }
        public Builder active(boolean active) { response.active = active; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { response.createdAt = createdAt; return this; }

        public ItemResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getBaseUnitId() { return baseUnitId; }
    public String getBaseUnitName() { return baseUnitName; }
    public String getBaseUnitSymbol() { return baseUnitSymbol; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
