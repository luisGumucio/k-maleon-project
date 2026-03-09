package com.kmaleon.controller;

import com.kmaleon.dto.ShipmentDetailResponse;
import com.kmaleon.dto.ShipmentItemRequest;
import com.kmaleon.dto.ShipmentItemResponse;
import com.kmaleon.service.ShipmentItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipment-items")
public class ShipmentItemController {

    private final ShipmentItemService shipmentItemService;

    public ShipmentItemController(ShipmentItemService shipmentItemService) {
        this.shipmentItemService = shipmentItemService;
    }

    @GetMapping
    public ShipmentDetailResponse findByShipmentId(
            @RequestParam UUID shipmentId) {
        return shipmentItemService.findByShipmentId(shipmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentItemResponse create(@Valid @RequestBody ShipmentItemRequest request) {
        return shipmentItemService.create(request);
    }

    @PutMapping("/{id}")
    public ShipmentItemResponse update(@PathVariable UUID id,
                                       @Valid @RequestBody ShipmentItemRequest request) {
        return shipmentItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        shipmentItemService.delete(id);
    }
}
