package com.kmaleon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class UpdateMinQuantityRequest {
    @NotNull(message = "locationId es obligatorio")
    private UUID locationId;

    @NotNull(message = "minQuantity es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "La cantidad mínima debe ser 0 o mayor")
    private BigDecimal minQuantity;

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity;
    }
}
