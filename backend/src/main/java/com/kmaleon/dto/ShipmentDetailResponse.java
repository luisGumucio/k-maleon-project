package com.kmaleon.dto;

import com.kmaleon.model.Shipment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ShipmentDetailResponse {

    private UUID id;
    private Integer number;
    private String containerNumber;
    private String supplierName;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private List<ShipmentItemResponse> items;
    private BigDecimal totalAmount;

    private ShipmentDetailResponse() {}

    public static ShipmentDetailResponse from(Shipment shipment, List<ShipmentItemResponse> items) {
        ShipmentDetailResponse r = new ShipmentDetailResponse();
        r.id = shipment.getId();
        r.number = shipment.getNumber();
        r.containerNumber = shipment.getContainerNumber();
        r.supplierName = shipment.getSupplier().getName();
        r.departureDate = shipment.getDepartureDate();
        r.arrivalDate = shipment.getArrivalDate();
        r.items = items;
        r.totalAmount = items.stream()
                .map(ShipmentItemResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return r;
    }

    public UUID getId() { return id; }
    public Integer getNumber() { return number; }
    public String getContainerNumber() { return containerNumber; }
    public String getSupplierName() { return supplierName; }
    public LocalDate getDepartureDate() { return departureDate; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public List<ShipmentItemResponse> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
