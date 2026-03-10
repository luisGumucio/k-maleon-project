package com.kmaleon.controller;

import com.kmaleon.dto.*;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.InventoryMovementService;
import com.kmaleon.service.InventoryStockService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryStockService inventoryStockService;
    private final InventoryMovementService inventoryMovementService;

    public InventoryController(InventoryStockService inventoryStockService,
                                InventoryMovementService inventoryMovementService) {
        this.inventoryStockService = inventoryStockService;
        this.inventoryMovementService = inventoryMovementService;
    }

    @GetMapping("/stock")
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<ItemStockResponse> getStock(@AuthenticationPrincipal AuthenticatedUser caller) {
        return inventoryStockService.findAll(caller);
    }

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ALMACENERO_OR_ADMIN)
    public InventoryMovementResponse purchase(@Valid @RequestBody PurchaseRequest request) {
        return inventoryMovementService.purchase(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ALMACENERO_OR_ADMIN)
    public InventoryMovementResponse transfer(@Valid @RequestBody TransferRequestDto request) {
        return inventoryMovementService.transfer(request);
    }

    @PostMapping("/consumption")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ENCARGADO_OR_ADMIN)
    public InventoryMovementResponse consumption(@Valid @RequestBody ConsumptionRequest request) {
        return inventoryMovementService.consumption(request);
    }

    @PostMapping("/adjustment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public InventoryMovementResponse adjustment(@Valid @RequestBody AdjustmentRequest request) {
        return inventoryMovementService.adjustment(request);
    }

    @GetMapping("/movements")
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<InventoryMovementResponse> getMovements(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return inventoryMovementService.findAll(caller, type, itemId, locationId, from, to);
    }

    @PutMapping("/items/{itemId}/min-quantity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public void updateMinQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMinQuantityRequest request) {
        inventoryStockService.updateMinQuantity(itemId, request.getLocationId(), request.getMinQuantity());
    }
}
