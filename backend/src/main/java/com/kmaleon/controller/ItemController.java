package com.kmaleon.controller;

import com.kmaleon.dto.ItemRequest;
import com.kmaleon.dto.ItemResponse;
import com.kmaleon.dto.UnitConversionRequest;
import com.kmaleon.dto.UnitConversionResponse;
import com.kmaleon.security.Roles;
import com.kmaleon.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<ItemResponse> findAll(@RequestParam(required = false) Boolean active) {
        return itemService.findAll(active);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public ItemResponse create(@Valid @RequestBody ItemRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public ItemResponse update(@PathVariable UUID id,
                               @Valid @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }

    // --- Conversions ---

    @GetMapping("/{itemId}/conversions")
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<UnitConversionResponse> findConversions(@PathVariable UUID itemId) {
        return itemService.findConversions(itemId);
    }

    @PostMapping("/{itemId}/conversions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public UnitConversionResponse addConversion(@PathVariable UUID itemId,
                                                @Valid @RequestBody UnitConversionRequest request) {
        return itemService.addConversion(itemId, request);
    }

    @DeleteMapping("/{itemId}/conversions/{conversionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public void deleteConversion(@PathVariable UUID itemId,
                                 @PathVariable UUID conversionId) {
        itemService.deleteConversion(itemId, conversionId);
    }
}
