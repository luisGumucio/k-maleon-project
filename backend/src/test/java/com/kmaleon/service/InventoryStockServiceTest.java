package com.kmaleon.service;

import com.kmaleon.dto.ItemStockResponse;
import com.kmaleon.model.InventoryStock;
import com.kmaleon.model.Item;
import com.kmaleon.model.Location;
import com.kmaleon.model.Unit;
import com.kmaleon.repository.InventoryStockRepository;
import com.kmaleon.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryStockServiceTest {

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @InjectMocks
    private InventoryStockService inventoryStockService;

    private Item item;
    private Unit baseUnit;
    private Location warehouse;
    private Location branch;
    private AuthenticatedUser adminCaller;
    private AuthenticatedUser encargadoCaller;

    @BeforeEach
    void setUp() {
        baseUnit = new Unit();
        baseUnit.setId(UUID.randomUUID());
        baseUnit.setName("Kilogramo");
        baseUnit.setSymbol("kg");

        item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Arroz");
        item.setBaseUnit(baseUnit);
        item.setActive(true);

        warehouse = new Location();
        warehouse.setId(UUID.randomUUID());
        warehouse.setName("Bodega Central");
        warehouse.setType("warehouse");
        warehouse.setActive(true);

        branch = new Location();
        branch.setId(UUID.randomUUID());
        branch.setName("Sucursal Norte");
        branch.setType("branch");
        branch.setActive(true);

        adminCaller = new AuthenticatedUser(UUID.randomUUID(), "inventory_admin", "Admin", null);
        encargadoCaller = new AuthenticatedUser(UUID.randomUUID(), "encargado_sucursal", "Encargado", branch.getId());
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenFindAll_asAdmin_thenReturnsAllLocations() {
        InventoryStock warehouseStock = buildStock(item, warehouse, new BigDecimal("80"));
        InventoryStock branchStock = buildStock(item, branch, new BigDecimal("20"));
        when(inventoryStockRepository.findAll()).thenReturn(List.of(warehouseStock, branchStock));

        List<ItemStockResponse> result = inventoryStockService.findAll(adminCaller);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocations()).hasSize(2);
        assertThat(result.get(0).getTotalQuantity()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    void whenFindAll_asEncargado_thenReturnsOnlyHisLocation() {
        InventoryStock warehouseStock = buildStock(item, warehouse, new BigDecimal("80"));
        InventoryStock branchStock = buildStock(item, branch, new BigDecimal("20"));
        when(inventoryStockRepository.findAll()).thenReturn(List.of(warehouseStock, branchStock));

        List<ItemStockResponse> result = inventoryStockService.findAll(encargadoCaller);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocations()).hasSize(1);
        assertThat(result.get(0).getLocations().get(0).getLocationId()).isEqualTo(branch.getId());
    }

    @Test
    void whenFindAll_asEncargado_andItemNotInHisBranch_thenItemIsExcluded() {
        Location otherBranch = new Location();
        otherBranch.setId(UUID.randomUUID());
        otherBranch.setName("Sucursal Sur");
        otherBranch.setType("branch");

        InventoryStock otherBranchStock = buildStock(item, otherBranch, new BigDecimal("15"));
        when(inventoryStockRepository.findAll()).thenReturn(List.of(otherBranchStock));

        List<ItemStockResponse> result = inventoryStockService.findAll(encargadoCaller);

        assertThat(result).isEmpty();
    }

    @Test
    void whenFindAll_andEmpty_thenReturnsEmptyList() {
        when(inventoryStockRepository.findAll()).thenReturn(List.of());

        List<ItemStockResponse> result = inventoryStockService.findAll(adminCaller);

        assertThat(result).isEmpty();
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenFindAll_asAdmin_andLowStock_thenLowStockFlagIsTrue() {
        InventoryStock stock = buildStock(item, warehouse, new BigDecimal("3"));
        stock.setMinQuantity(new BigDecimal("10")); // quantity < minQuantity → lowStock
        when(inventoryStockRepository.findAll()).thenReturn(List.of(stock));

        List<ItemStockResponse> result = inventoryStockService.findAll(adminCaller);

        assertThat(result.get(0).getLocations().get(0).isLowStock()).isTrue();
    }

    @Test
    void whenUpdateMinQuantity_thenSavesUpdatedValue() {
        InventoryStock stock = buildStock(item, warehouse, new BigDecimal("50"));
        when(inventoryStockRepository.findByItemIdAndLocationId(item.getId(), warehouse.getId()))
                .thenReturn(Optional.of(stock));
        when(inventoryStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventoryStockService.updateMinQuantity(item.getId(), warehouse.getId(), new BigDecimal("15"));

        assertThat(stock.getMinQuantity()).isEqualByComparingTo(new BigDecimal("15"));
        verify(inventoryStockRepository).save(stock);
    }

    @Test
    void whenUpdateMinQuantity_andStockNotFound_thenThrows() {
        when(inventoryStockRepository.findByItemIdAndLocationId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                inventoryStockService.updateMinQuantity(item.getId(), warehouse.getId(), new BigDecimal("10")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private InventoryStock buildStock(Item item, Location location, BigDecimal quantity) {
        InventoryStock stock = new InventoryStock();
        stock.setItem(item);
        stock.setLocation(location);
        stock.setQuantity(quantity);
        stock.setMinQuantity(BigDecimal.ZERO);
        return stock;
    }
}
