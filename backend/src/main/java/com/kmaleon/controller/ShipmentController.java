package com.kmaleon.controller;

import com.kmaleon.dto.ShipmentRequest;
import com.kmaleon.dto.ShipmentResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentResponse> findAll(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String containerNumber,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return shipmentService.findAll(caller.getId(), caller.getRole(), supplierId, containerNumber, from, to);
    }

    @GetMapping("/{id}")
    public ShipmentResponse findById(@PathVariable UUID id,
                                     @AuthenticationPrincipal AuthenticatedUser caller) {
        return shipmentService.findById(id, caller.getId(), caller.getRole());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(@Valid @RequestBody ShipmentRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        return shipmentService.create(caller.getId(), request);
    }

    @PutMapping("/{id}")
    public ShipmentResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody ShipmentRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        return shipmentService.update(id, caller.getId(), caller.getRole(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal AuthenticatedUser caller) {
        shipmentService.delete(id, caller.getId(), caller.getRole());
    }
}
