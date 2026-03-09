package com.kmaleon.controller;

import com.kmaleon.dto.ShipmentRequest;
import com.kmaleon.dto.ShipmentResponse;
import com.kmaleon.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentResponse> findAll(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String containerNumber,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return shipmentService.findAll(supplierId, containerNumber, from, to);
    }

    @GetMapping("/{id}")
    public ShipmentResponse findById(@PathVariable UUID id) {
        return shipmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(@Valid @RequestBody ShipmentRequest request) {
        return shipmentService.create(request);
    }

    @PutMapping("/{id}")
    public ShipmentResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody ShipmentRequest request) {
        return shipmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        shipmentService.delete(id);
    }
}
