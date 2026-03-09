package com.kmaleon.controller;

import com.kmaleon.dto.*;
import com.kmaleon.service.InventoryMovementService;
import com.kmaleon.service.InventoryStockService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    public List<ItemStockResponse> getStock() {
        return inventoryStockService.findAll();
    }

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse purchase(@Valid @RequestBody PurchaseRequest request) {
        return inventoryMovementService.purchase(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse transfer(@Valid @RequestBody TransferRequestDto request) {
        return inventoryMovementService.transfer(request);
    }

    @PostMapping("/consumption")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse consumption(@Valid @RequestBody ConsumptionRequest request) {
        return inventoryMovementService.consumption(request);
    }

    @PostMapping("/adjustment")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse adjustment(@Valid @RequestBody AdjustmentRequest request) {
        return inventoryMovementService.adjustment(request);
    }

    @GetMapping("/movements")
    public List<InventoryMovementResponse> getMovements(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return inventoryMovementService.findAll(type, itemId, locationId, from, to);
    }

    @PutMapping("/items/{itemId}/min-quantity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMinQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMinQuantityRequest request) {
        inventoryStockService.updateMinQuantity(itemId, request.getLocationId(), request.getMinQuantity());
    }
}
