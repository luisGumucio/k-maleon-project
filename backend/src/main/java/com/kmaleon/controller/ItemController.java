package com.kmaleon.controller;

import com.kmaleon.dto.ItemRequest;
import com.kmaleon.dto.ItemResponse;
import com.kmaleon.dto.UnitConversionRequest;
import com.kmaleon.dto.UnitConversionResponse;
import com.kmaleon.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public List<ItemResponse> findAll(@RequestParam(required = false) Boolean active) {
        return itemService.findAll(active);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody ItemRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable UUID id,
                               @Valid @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }

    // --- Conversions ---

    @GetMapping("/{itemId}/conversions")
    public List<UnitConversionResponse> findConversions(@PathVariable UUID itemId) {
        return itemService.findConversions(itemId);
    }

    @PostMapping("/{itemId}/conversions")
    @ResponseStatus(HttpStatus.CREATED)
    public UnitConversionResponse addConversion(@PathVariable UUID itemId,
                                                @Valid @RequestBody UnitConversionRequest request) {
        return itemService.addConversion(itemId, request);
    }

    @DeleteMapping("/{itemId}/conversions/{conversionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversion(@PathVariable UUID itemId,
                                 @PathVariable UUID conversionId) {
        itemService.deleteConversion(itemId, conversionId);
    }
}
