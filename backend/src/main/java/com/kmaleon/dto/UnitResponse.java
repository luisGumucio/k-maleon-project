package com.kmaleon.dto;

import com.kmaleon.model.Unit;

import java.util.UUID;

public class UnitResponse {

    private UUID id;
    private String name;
    private String symbol;

    private UnitResponse() {}

    public static UnitResponse from(Unit unit) {
        return new Builder()
                .id(unit.getId())
                .name(unit.getName())
                .symbol(unit.getSymbol())
                .build();
    }

    public static class Builder {
        private final UnitResponse response = new UnitResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder symbol(String symbol) { response.symbol = symbol; return this; }

        public UnitResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
}
