package com.kmaleon.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ItemStockResponse {

    private UUID itemId;
    private String itemName;
    private String baseUnitSymbol;
    private List<StockLocationEntry> locations;
    private BigDecimal totalQuantity;

    private ItemStockResponse() {}

    public static class Builder {
        private final ItemStockResponse response = new ItemStockResponse();

        public Builder itemId(UUID itemId) { response.itemId = itemId; return this; }
        public Builder itemName(String itemName) { response.itemName = itemName; return this; }
        public Builder baseUnitSymbol(String baseUnitSymbol) { response.baseUnitSymbol = baseUnitSymbol; return this; }
        public Builder locations(List<StockLocationEntry> locations) { response.locations = locations; return this; }
        public Builder totalQuantity(BigDecimal totalQuantity) { response.totalQuantity = totalQuantity; return this; }

        public ItemStockResponse build() { return response; }
    }

    public UUID getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getBaseUnitSymbol() { return baseUnitSymbol; }
    public List<StockLocationEntry> getLocations() { return locations; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
}
