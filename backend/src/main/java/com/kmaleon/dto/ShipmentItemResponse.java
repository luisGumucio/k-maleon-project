package com.kmaleon.dto;

import com.kmaleon.model.ShipmentItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ShipmentItemResponse {

    private UUID id;
    private UUID shipmentId;
    private String containerNumber;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private ShipmentItemResponse() {}

    public static ShipmentItemResponse from(ShipmentItem item) {
        ShipmentItemResponse r = new ShipmentItemResponse();
        r.id = item.getId();
        r.shipmentId = item.getShipment().getId();
        r.containerNumber = item.getShipment().getContainerNumber();
        r.description = item.getDescription();
        r.quantity = item.getQuantity();
        r.unitPrice = item.getUnitPrice();
        r.amount = item.getAmount();
        r.createdAt = item.getCreatedAt();
        r.updatedAt = item.getUpdatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getShipmentId() { return shipmentId; }
    public String getContainerNumber() { return containerNumber; }
    public String getDescription() { return description; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
