package com.kmaleon.dto;

import com.kmaleon.model.Shipment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ShipmentResponse {

    private UUID id;
    private Integer number;
    private UUID supplierId;
    private String supplierName;
    private LocalDate departureDate;
    private String containerNumber;
    private Integer quantity;
    private String productDetails;
    private LocalDate arrivalDate;
    private String documentUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private ShipmentResponse() {}

    public static ShipmentResponse from(Shipment shipment) {
        ShipmentResponse r = new ShipmentResponse();
        r.id = shipment.getId();
        r.number = shipment.getNumber();
        r.supplierId = shipment.getSupplier().getId();
        r.supplierName = shipment.getSupplier().getName();
        r.departureDate = shipment.getDepartureDate();
        r.containerNumber = shipment.getContainerNumber();
        r.quantity = shipment.getQuantity();
        r.productDetails = shipment.getProductDetails();
        r.arrivalDate = shipment.getArrivalDate();
        r.documentUrl = shipment.getDocumentUrl();
        r.createdAt = shipment.getCreatedAt();
        r.updatedAt = shipment.getUpdatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public Integer getNumber() { return number; }
    public UUID getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public LocalDate getDepartureDate() { return departureDate; }
    public String getContainerNumber() { return containerNumber; }
    public Integer getQuantity() { return quantity; }
    public String getProductDetails() { return productDetails; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public String getDocumentUrl() { return documentUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
