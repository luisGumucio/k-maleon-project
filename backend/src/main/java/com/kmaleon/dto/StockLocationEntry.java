package com.kmaleon.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class StockLocationEntry {

    private UUID locationId;
    private String locationName;
    private String locationType;
    private BigDecimal quantity;
    private BigDecimal minQuantity;
    private boolean lowStock;

    private StockLocationEntry() {}

    public static class Builder {
        private final StockLocationEntry entry = new StockLocationEntry();

        public Builder locationId(UUID locationId) { entry.locationId = locationId; return this; }
        public Builder locationName(String locationName) { entry.locationName = locationName; return this; }
        public Builder locationType(String locationType) { entry.locationType = locationType; return this; }
        public Builder quantity(BigDecimal quantity) { entry.quantity = quantity; return this; }
        public Builder minQuantity(BigDecimal minQuantity) { entry.minQuantity = minQuantity; return this; }
        public Builder lowStock(boolean lowStock) { entry.lowStock = lowStock; return this; }

        public StockLocationEntry build() { return entry; }
    }

    public UUID getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getLocationType() { return locationType; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public boolean isLowStock() { return lowStock; }
}
