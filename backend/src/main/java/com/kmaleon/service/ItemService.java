package com.kmaleon.service;

import com.kmaleon.dto.ItemRequest;
import com.kmaleon.dto.ItemResponse;
import com.kmaleon.dto.UnitConversionRequest;
import com.kmaleon.dto.UnitConversionResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Item;
import com.kmaleon.model.Unit;
import com.kmaleon.model.UnitConversion;
import com.kmaleon.repository.ItemRepository;
import com.kmaleon.repository.UnitConversionRepository;
import com.kmaleon.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UnitRepository unitRepository;
    private final UnitConversionRepository unitConversionRepository;

    public ItemService(ItemRepository itemRepository,
                       UnitRepository unitRepository,
                       UnitConversionRepository unitConversionRepository) {
        this.itemRepository = itemRepository;
        this.unitRepository = unitRepository;
        this.unitConversionRepository = unitConversionRepository;
    }

    public List<ItemResponse> findAll(Boolean active) {
        List<Item> items = (active != null && active)
                ? itemRepository.findByActiveTrue()
                : itemRepository.findAll();
        return items.stream().map(ItemResponse::from).toList();
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        Unit baseUnit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getBaseUnitId()));

        Item item = new Item();
        item.setName(request.getName());
        item.setBaseUnit(baseUnit);
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(UUID id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));

        Unit baseUnit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getBaseUnitId()));

        item.setName(request.getName());
        item.setBaseUnit(baseUnit);
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        item.setActive(false);
        itemRepository.save(item);
    }

    // --- Conversions ---

    public List<UnitConversionResponse> findConversions(UUID itemId) {
        itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        return unitConversionRepository.findByItemId(itemId).stream()
                .map(UnitConversionResponse::from)
                .toList();
    }

    @Transactional
    public UnitConversionResponse addConversion(UUID itemId, UnitConversionRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        Unit fromUnit = unitRepository.findById(request.getFromUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getFromUnitId()));

        Unit toUnit = unitRepository.findById(request.getToUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getToUnitId()));

        if (unitConversionRepository.existsByItemIdAndFromUnitId(itemId, request.getFromUnitId())) {
            throw new IllegalArgumentException("Conversion already exists for this item and unit");
        }

        UnitConversion conversion = new UnitConversion();
        conversion.setItem(item);
        conversion.setFromUnit(fromUnit);
        conversion.setToUnit(toUnit);
        conversion.setFactor(request.getFactor());
        return UnitConversionResponse.from(unitConversionRepository.save(conversion));
    }

    @Transactional
    public void deleteConversion(UUID itemId, UUID conversionId) {
        itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        UnitConversion conversion = unitConversionRepository.findById(conversionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversion not found: " + conversionId));
        unitConversionRepository.delete(conversion);
    }
}
