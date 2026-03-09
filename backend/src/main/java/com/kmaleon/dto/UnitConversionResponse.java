package com.kmaleon.dto;

import com.kmaleon.model.UnitConversion;

import java.math.BigDecimal;
import java.util.UUID;

public class UnitConversionResponse {

    private UUID id;
    private UUID itemId;
    private UUID fromUnitId;
    private String fromUnitName;
    private String fromUnitSymbol;
    private UUID toUnitId;
    private String toUnitName;
    private String toUnitSymbol;
    private BigDecimal factor;

    private UnitConversionResponse() {}

    public static UnitConversionResponse from(UnitConversion conversion) {
        return new Builder()
                .id(conversion.getId())
                .itemId(conversion.getItem().getId())
                .fromUnitId(conversion.getFromUnit().getId())
                .fromUnitName(conversion.getFromUnit().getName())
                .fromUnitSymbol(conversion.getFromUnit().getSymbol())
                .toUnitId(conversion.getToUnit().getId())
                .toUnitName(conversion.getToUnit().getName())
                .toUnitSymbol(conversion.getToUnit().getSymbol())
                .factor(conversion.getFactor())
                .build();
    }

    public static class Builder {
        private final UnitConversionResponse response = new UnitConversionResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder itemId(UUID itemId) { response.itemId = itemId; return this; }
        public Builder fromUnitId(UUID fromUnitId) { response.fromUnitId = fromUnitId; return this; }
        public Builder fromUnitName(String fromUnitName) { response.fromUnitName = fromUnitName; return this; }
        public Builder fromUnitSymbol(String fromUnitSymbol) { response.fromUnitSymbol = fromUnitSymbol; return this; }
        public Builder toUnitId(UUID toUnitId) { response.toUnitId = toUnitId; return this; }
        public Builder toUnitName(String toUnitName) { response.toUnitName = toUnitName; return this; }
        public Builder toUnitSymbol(String toUnitSymbol) { response.toUnitSymbol = toUnitSymbol; return this; }
        public Builder factor(BigDecimal factor) { response.factor = factor; return this; }

        public UnitConversionResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public UUID getItemId() { return itemId; }
    public UUID getFromUnitId() { return fromUnitId; }
    public String getFromUnitName() { return fromUnitName; }
    public String getFromUnitSymbol() { return fromUnitSymbol; }
    public UUID getToUnitId() { return toUnitId; }
    public String getToUnitName() { return toUnitName; }
    public String getToUnitSymbol() { return toUnitSymbol; }
    public BigDecimal getFactor() { return factor; }
}
