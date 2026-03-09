package com.kmaleon.service;

import com.kmaleon.dto.*;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.*;
import com.kmaleon.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryStockRepository stockRepository;
    private final ItemRepository itemRepository;
    private final UnitRepository unitRepository;
    private final LocationRepository locationRepository;
    private final UnitConversionRepository conversionRepository;

    public InventoryMovementService(InventoryMovementRepository movementRepository,
                                    InventoryStockRepository stockRepository,
                                    ItemRepository itemRepository,
                                    UnitRepository unitRepository,
                                    LocationRepository locationRepository,
                                    UnitConversionRepository conversionRepository) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.itemRepository = itemRepository;
        this.unitRepository = unitRepository;
        this.locationRepository = locationRepository;
        this.conversionRepository = conversionRepository;
    }

    // --- Purchase ---

    @Transactional
    public InventoryMovementResponse purchase(PurchaseRequest request) {
        Item item = findActiveItem(request.getItemId());
        Unit unit = findUnit(request.getUnitId());
        BigDecimal quantityBase = convertToBase(item, unit, request.getQuantity());

        Location warehouse = findWarehouse();

        addStock(item, warehouse, quantityBase);

        InventoryMovement movement = buildMovement(item, unit, request.getQuantity(),
                quantityBase, "purchase", null, warehouse, request.getNotes());

        return InventoryMovementResponse.from(movementRepository.save(movement));
    }

    // --- Transfer ---

    @Transactional
    public InventoryMovementResponse transfer(TransferRequestDto request) {
        Item item = findActiveItem(request.getItemId());
        Unit unit = findUnit(request.getUnitId());
        BigDecimal quantityBase = convertToBase(item, unit, request.getQuantity());

        Location warehouse = findWarehouse();
        Location destination = locationRepository.findById(request.getLocationToId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.getLocationToId()));

        if (!destination.isActive()) {
            throw new IllegalArgumentException("Destination location is inactive");
        }

        deductStock(item, warehouse, quantityBase);
        addStock(item, destination, quantityBase);

        InventoryMovement movement = buildMovement(item, unit, request.getQuantity(),
                quantityBase, "transfer", warehouse, destination, request.getNotes());

        return InventoryMovementResponse.from(movementRepository.save(movement));
    }

    // --- Consumption ---

    @Transactional
    public InventoryMovementResponse consumption(ConsumptionRequest request) {
        Item item = findActiveItem(request.getItemId());
        Unit unit = findUnit(request.getUnitId());
        BigDecimal quantityBase = convertToBase(item, unit, request.getQuantity());

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.getLocationId()));

        deductStock(item, location, quantityBase);

        InventoryMovement movement = buildMovement(item, unit, request.getQuantity(),
                quantityBase, "consumption", location, null, request.getNotes());

        return InventoryMovementResponse.from(movementRepository.save(movement));
    }

    // --- Adjustment ---

    @Transactional
    public InventoryMovementResponse adjustment(AdjustmentRequest request) {
        Item item = findActiveItem(request.getItemId());
        Unit unit = findUnit(request.getUnitId());
        BigDecimal quantityBase = convertToBase(item, unit, request.getQuantity());

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.getLocationId()));

        InventoryStock stock = stockRepository.findByItemIdAndLocationId(item.getId(), location.getId())
                .orElseGet(() -> {
                    InventoryStock s = new InventoryStock();
                    s.setItem(item);
                    s.setLocation(location);
                    s.setQuantity(BigDecimal.ZERO);
                    s.setMinQuantity(BigDecimal.ZERO);
                    return s;
                });

        BigDecimal newQuantity = stock.getQuantity().add(quantityBase);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Adjustment would result in negative stock. Current: " + stock.getQuantity());
        }
        stock.setQuantity(newQuantity);
        stockRepository.save(stock);

        InventoryMovement movement = buildMovement(item, unit, request.getQuantity(),
                quantityBase, "adjustment", null, location, request.getNotes());

        return InventoryMovementResponse.from(movementRepository.save(movement));
    }

    // --- History ---

    public List<InventoryMovementResponse> findAll(String type, UUID itemId,
                                                   UUID locationId, LocalDate from, LocalDate to) {
        Specification<InventoryMovement> spec = buildSpec(type, itemId, locationId, from, to);
        return movementRepository.findAll(spec).stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }

    // --- Helpers ---

    private Item findActiveItem(UUID itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        if (!item.isActive()) {
            throw new IllegalArgumentException("Item is inactive: " + itemId);
        }
        return item;
    }

    private Unit findUnit(UUID unitId) {
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
    }

    private Location findWarehouse() {
        return locationRepository.findByTypeAndActiveTrue("warehouse").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active warehouse found"));
    }

    private BigDecimal convertToBase(Item item, Unit unit, BigDecimal quantity) {
        // Si la unidad ya es la unidad base del item, no se necesita conversión
        if (unit.getId().equals(item.getBaseUnit().getId())) {
            return quantity;
        }
        UnitConversion conversion = conversionRepository
                .findByItemId(item.getId()).stream()
                .filter(c -> c.getFromUnit().getId().equals(unit.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No conversion found for item '" + item.getName() +
                        "' with unit '" + unit.getName() + "'"));
        return quantity.multiply(conversion.getFactor());
    }

    private void addStock(Item item, Location location, BigDecimal quantityBase) {
        InventoryStock stock = stockRepository.findByItemIdAndLocationId(item.getId(), location.getId())
                .orElseGet(() -> {
                    InventoryStock s = new InventoryStock();
                    s.setItem(item);
                    s.setLocation(location);
                    s.setQuantity(BigDecimal.ZERO);
                    s.setMinQuantity(BigDecimal.ZERO);
                    return s;
                });
        stock.setQuantity(stock.getQuantity().add(quantityBase));
        stockRepository.save(stock);
    }

    private void deductStock(Item item, Location location, BigDecimal quantityBase) {
        InventoryStock stock = stockRepository.findByItemIdAndLocationId(item.getId(), location.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No stock found for item '" + item.getName() +
                        "' at location '" + location.getName() + "'"));
        if (stock.getQuantity().compareTo(quantityBase) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for item '" + item.getName() +
                    "'. Available: " + stock.getQuantity() +
                    " " + item.getBaseUnit().getSymbol());
        }
        stock.setQuantity(stock.getQuantity().subtract(quantityBase));
        stockRepository.save(stock);
    }

    private InventoryMovement buildMovement(Item item, Unit unit, BigDecimal quantity,
                                            BigDecimal quantityBase, String type,
                                            Location from, Location to, String notes) {
        InventoryMovement m = new InventoryMovement();
        m.setItem(item);
        m.setUnit(unit);
        m.setQuantity(quantity);
        m.setQuantityBase(quantityBase);
        m.setMovementType(type);
        m.setLocationFrom(from);
        m.setLocationTo(to);
        m.setNotes(notes);
        return m;
    }

    private Specification<InventoryMovement> buildSpec(String type, UUID itemId,
                                                       UUID locationId, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("movementType"), type));
            }
            if (itemId != null) {
                predicates.add(cb.equal(root.get("item").get("id"), itemId));
            }
            if (locationId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("locationFrom").get("id"), locationId),
                        cb.equal(root.get("locationTo").get("id"), locationId)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDate.class), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDate.class), to));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
