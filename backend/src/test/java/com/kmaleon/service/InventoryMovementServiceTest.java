package com.kmaleon.service;

import com.kmaleon.dto.*;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.*;
import com.kmaleon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock private InventoryMovementRepository movementRepository;
    @Mock private InventoryStockRepository stockRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private UnitConversionRepository conversionRepository;

    @InjectMocks
    private InventoryMovementService service;

    private Unit baseUnit;
    private Unit altUnit;
    private Item item;
    private Location warehouse;
    private Location branch;
    private UnitConversion conversion;

    @BeforeEach
    void setUp() {
        baseUnit = buildUnit("gramo", "g");
        altUnit  = buildUnit("vaso", "vaso");

        item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Arroz");
        item.setBaseUnit(baseUnit);
        item.setActive(true);

        warehouse = buildLocation("Bodega Central", "warehouse");
        branch    = buildLocation("Sucursal Norte", "branch");

        conversion = new UnitConversion();
        conversion.setId(UUID.randomUUID());
        conversion.setItem(item);
        conversion.setFromUnit(altUnit);
        conversion.setToUnit(baseUnit);
        conversion.setFactor(new BigDecimal("200"));
    }

    // -------------------------
    // Happy path — Purchase
    // -------------------------

    @Test
    void whenPurchase_withBaseUnit_thenQuantityBaseEqualsQuantity() {
        PurchaseRequest request = buildPurchaseRequest(item.getId(), baseUnit.getId(), "5");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(any(), any())).thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        InventoryMovementResponse response = service.purchase(request);

        assertThat(response.getQuantityBase()).isEqualByComparingTo("5");
        assertThat(response.getMovementType()).isEqualTo("purchase");
    }

    @Test
    void whenPurchase_withAltUnit_thenQuantityBaseIsConverted() {
        PurchaseRequest request = buildPurchaseRequest(item.getId(), altUnit.getId(), "2");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(altUnit.getId())).thenReturn(Optional.of(altUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(conversionRepository.findByItemId(item.getId())).thenReturn(List.of(conversion));
        when(stockRepository.findByItemIdAndLocationId(any(), any())).thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        InventoryMovementResponse response = service.purchase(request);

        // 2 vasos × 200 g/vaso = 400 g
        assertThat(response.getQuantityBase()).isEqualByComparingTo("400");
    }

    @Test
    void whenPurchase_thenStockIsCreatedForWarehouse() {
        PurchaseRequest request = buildPurchaseRequest(item.getId(), baseUnit.getId(), "10");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(any(), any())).thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        service.purchase(request);

        ArgumentCaptor<InventoryStock> captor = ArgumentCaptor.forClass(InventoryStock.class);
        verify(stockRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo("10");
        assertThat(captor.getValue().getLocation()).isEqualTo(warehouse);
    }

    @Test
    void whenPurchase_andStockAlreadyExists_thenQuantityAccumulates() {
        InventoryStock existing = buildStock(item, warehouse, "50");
        PurchaseRequest request = buildPurchaseRequest(item.getId(), baseUnit.getId(), "20");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(any(), any())).thenReturn(Optional.of(existing));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        service.purchase(request);

        ArgumentCaptor<InventoryStock> captor = ArgumentCaptor.forClass(InventoryStock.class);
        verify(stockRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo("70");
    }

    // -------------------------
    // Happy path — Transfer
    // -------------------------

    @Test
    void whenTransfer_thenDeductsWarehouseAndAddsBranch() {
        InventoryStock warehouseStock = buildStock(item, warehouse, "100");
        TransferRequestDto request = buildTransferRequest(item.getId(), baseUnit.getId(), "30", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(warehouseStock));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), branch.getId()))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        InventoryMovementResponse response = service.transfer(request);

        assertThat(response.getMovementType()).isEqualTo("transfer");
        assertThat(warehouseStock.getQuantity()).isEqualByComparingTo("70");
    }

    // -------------------------
    // Happy path — Consumption
    // -------------------------

    @Test
    void whenConsumption_thenDeductsFromLocation() {
        InventoryStock branchStock = buildStock(item, branch, "50");
        ConsumptionRequest request = buildConsumptionRequest(item.getId(), baseUnit.getId(), "10", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), branch.getId()))
                .thenReturn(Optional.of(branchStock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        service.consumption(request);

        assertThat(branchStock.getQuantity()).isEqualByComparingTo("40");
    }

    // -------------------------
    // Happy path — Adjustment
    // -------------------------

    @Test
    void whenAdjustment_positive_thenAddsStock() {
        InventoryStock stock = buildStock(item, warehouse, "100");
        AdjustmentRequest request = buildAdjustmentRequest(item.getId(), warehouse.getId(), baseUnit.getId(), "20");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        service.adjustment(request);

        assertThat(stock.getQuantity()).isEqualByComparingTo("120");
    }

    @Test
    void whenAdjustment_negative_thenDeductsStock() {
        InventoryStock stock = buildStock(item, warehouse, "100");
        AdjustmentRequest request = buildAdjustmentRequest(item.getId(), warehouse.getId(), baseUnit.getId(), "-30");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        service.adjustment(request);

        assertThat(stock.getQuantity()).isEqualByComparingTo("70");
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenPurchase_andItemNotFound_thenThrows() {
        PurchaseRequest request = buildPurchaseRequest(UUID.randomUUID(), baseUnit.getId(), "5");
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(movementRepository, never()).save(any());
    }

    @Test
    void whenPurchase_andItemInactive_thenThrows() {
        item.setActive(false);
        PurchaseRequest request = buildPurchaseRequest(item.getId(), baseUnit.getId(), "5");
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.purchase(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void whenPurchase_andNoConversionFound_thenThrows() {
        PurchaseRequest request = buildPurchaseRequest(item.getId(), altUnit.getId(), "2");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(altUnit.getId())).thenReturn(Optional.of(altUnit));
        when(conversionRepository.findByItemId(item.getId())).thenReturn(List.of()); // sin conversión

        assertThatThrownBy(() -> service.purchase(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No conversion found");
    }

    @Test
    void whenPurchase_andNoWarehouseFound_thenThrows() {
        PurchaseRequest request = buildPurchaseRequest(item.getId(), baseUnit.getId(), "5");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of());

        assertThatThrownBy(() -> service.purchase(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No active warehouse");
    }

    @Test
    void whenTransfer_andInsufficientStock_thenThrows() {
        InventoryStock warehouseStock = buildStock(item, warehouse, "10");
        TransferRequestDto request = buildTransferRequest(item.getId(), baseUnit.getId(), "50", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(warehouseStock));

        assertThatThrownBy(() -> service.transfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(movementRepository, never()).save(any());
    }

    @Test
    void whenTransfer_andDestinationInactive_thenThrows() {
        branch.setActive(false);
        TransferRequestDto request = buildTransferRequest(item.getId(), baseUnit.getId(), "10", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        assertThatThrownBy(() -> service.transfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void whenConsumption_andInsufficientStock_thenThrows() {
        InventoryStock branchStock = buildStock(item, branch, "5");
        ConsumptionRequest request = buildConsumptionRequest(item.getId(), baseUnit.getId(), "20", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), branch.getId()))
                .thenReturn(Optional.of(branchStock));

        assertThatThrownBy(() -> service.consumption(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void whenAdjustment_andResultsInNegativeStock_thenThrows() {
        InventoryStock stock = buildStock(item, warehouse, "10");
        AdjustmentRequest request = buildAdjustmentRequest(item.getId(), warehouse.getId(), baseUnit.getId(), "-50");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> service.adjustment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative stock");
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenPurchase_withDecimalQuantity_thenConvertsCorrectly() {
        // 0.5 vasos × 200 g/vaso = 100 g
        PurchaseRequest request = buildPurchaseRequest(item.getId(), altUnit.getId(), "0.5");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(altUnit.getId())).thenReturn(Optional.of(altUnit));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(conversionRepository.findByItemId(item.getId())).thenReturn(List.of(conversion));
        when(stockRepository.findByItemIdAndLocationId(any(), any())).thenReturn(Optional.empty());
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        InventoryMovementResponse response = service.purchase(request);

        assertThat(response.getQuantityBase()).isEqualByComparingTo("100");
    }

    @Test
    void whenConsumption_andNoStockEntry_thenThrows() {
        ConsumptionRequest request = buildConsumptionRequest(item.getId(), baseUnit.getId(), "5", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(stockRepository.findByItemIdAndLocationId(item.getId(), branch.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumption(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No stock found");
    }

    // -------------------------
    // Helpers
    // -------------------------

    private Unit buildUnit(String name, String symbol) {
        Unit u = new Unit();
        u.setId(UUID.randomUUID());
        u.setName(name);
        u.setSymbol(symbol);
        return u;
    }

    private Location buildLocation(String name, String type) {
        Location l = new Location();
        l.setId(UUID.randomUUID());
        l.setName(name);
        l.setType(type);
        l.setActive(true);
        return l;
    }

    private InventoryStock buildStock(Item item, Location location, String quantity) {
        InventoryStock s = new InventoryStock();
        s.setId(UUID.randomUUID());
        s.setItem(item);
        s.setLocation(location);
        s.setQuantity(new BigDecimal(quantity));
        s.setMinQuantity(BigDecimal.ZERO);
        return s;
    }

    private PurchaseRequest buildPurchaseRequest(UUID itemId, UUID unitId, String quantity) {
        PurchaseRequest r = new PurchaseRequest();
        r.setItemId(itemId);
        r.setUnitId(unitId);
        r.setQuantity(new BigDecimal(quantity));
        return r;
    }

    private TransferRequestDto buildTransferRequest(UUID itemId, UUID unitId, String quantity, UUID locationToId) {
        TransferRequestDto r = new TransferRequestDto();
        r.setItemId(itemId);
        r.setUnitId(unitId);
        r.setQuantity(new BigDecimal(quantity));
        r.setLocationToId(locationToId);
        return r;
    }

    private ConsumptionRequest buildConsumptionRequest(UUID itemId, UUID unitId, String quantity, UUID locationId) {
        ConsumptionRequest r = new ConsumptionRequest();
        r.setItemId(itemId);
        r.setUnitId(unitId);
        r.setQuantity(new BigDecimal(quantity));
        r.setLocationId(locationId);
        return r;
    }

    private AdjustmentRequest buildAdjustmentRequest(UUID itemId, UUID locationId, UUID unitId, String quantity) {
        AdjustmentRequest r = new AdjustmentRequest();
        r.setItemId(itemId);
        r.setLocationId(locationId);
        r.setUnitId(unitId);
        r.setQuantity(new BigDecimal(quantity));
        r.setNotes("Ajuste de prueba");
        return r;
    }

    private InventoryMovement withId(InventoryMovement m) {
        m.setId(UUID.randomUUID());
        return m;
    }
}
