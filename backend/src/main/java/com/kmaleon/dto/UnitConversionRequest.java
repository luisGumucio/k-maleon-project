package com.kmaleon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class UnitConversionRequest {

    @NotNull(message = "fromUnitId is required")
    private UUID fromUnitId;

    @NotNull(message = "toUnitId is required")
    private UUID toUnitId;

    @NotNull(message = "factor is required")
    @DecimalMin(value = "0.000001", message = "factor must be greater than zero")
    private BigDecimal factor;

    public UUID getFromUnitId() { return fromUnitId; }
    public void setFromUnitId(UUID fromUnitId) { this.fromUnitId = fromUnitId; }

    public UUID getToUnitId() { return toUnitId; }
    public void setToUnitId(UUID toUnitId) { this.toUnitId = toUnitId; }

    public BigDecimal getFactor() { return factor; }
    public void setFactor(BigDecimal factor) { this.factor = factor; }
}
