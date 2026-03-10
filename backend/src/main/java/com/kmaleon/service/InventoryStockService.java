package com.kmaleon.service;

import com.kmaleon.dto.ItemStockResponse;
import com.kmaleon.dto.StockLocationEntry;
import com.kmaleon.model.InventoryStock;
import com.kmaleon.repository.InventoryStockRepository;
import com.kmaleon.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;

    public InventoryStockService(InventoryStockRepository inventoryStockRepository) {
        this.inventoryStockRepository = inventoryStockRepository;
    }

    public List<ItemStockResponse> findAll(AuthenticatedUser caller) {
        List<InventoryStock> allStock = inventoryStockRepository.findAll();

        Map<UUID, List<InventoryStock>> byItem = allStock.stream()
                .collect(Collectors.groupingBy(s -> s.getItem().getId()));

        boolean isEncargado = "encargado_sucursal".equals(caller.getRole());
        UUID callerLocationId = caller.getLocationId();

        return byItem.entrySet().stream()
                .map(entry -> buildItemStockResponse(entry.getValue(), isEncargado, callerLocationId))
                .filter(r -> !isEncargado || !r.getLocations().isEmpty())
                .toList();
    }

    public Optional<InventoryStock> findByItemAndLocation(UUID itemId, UUID locationId) {
        return inventoryStockRepository.findByItemIdAndLocationId(itemId, locationId);
    }

    public InventoryStock save(InventoryStock stock) {
        return inventoryStockRepository.save(stock);
    }
    
    public void updateMinQuantity(UUID itemId, UUID locationId, BigDecimal minQuantity) {
        InventoryStock stock = findByItemAndLocation(itemId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("No stock record found for item " + itemId + " at location " + locationId));
        
        stock.setMinQuantity(minQuantity);
        inventoryStockRepository.save(stock);
    }

    private ItemStockResponse buildItemStockResponse(List<InventoryStock> stocks,
                                                      boolean filterByLocation, UUID locationId) {
        InventoryStock first = stocks.get(0);

        List<StockLocationEntry> locationEntries = stocks.stream()
                .filter(s -> !filterByLocation || s.getLocation().getId().equals(locationId))
                .map(s -> new StockLocationEntry.Builder()
                        .locationId(s.getLocation().getId())
                        .locationName(s.getLocation().getName())
                        .locationType(s.getLocation().getType())
                        .quantity(s.getQuantity())
                        .minQuantity(s.getMinQuantity())
                        .lowStock(s.getQuantity().compareTo(s.getMinQuantity()) < 0)
                        .build())
                .toList();

        BigDecimal total = stocks.stream()
                .map(InventoryStock::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ItemStockResponse.Builder()
                .itemId(first.getItem().getId())
                .itemName(first.getItem().getName())
                .baseUnitSymbol(first.getItem().getBaseUnit().getSymbol())
                .locations(locationEntries)
                .totalQuantity(total)
                .build();
    }
}
