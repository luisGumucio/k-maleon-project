package com.kmaleon.service;

import com.kmaleon.dto.TransferRequestItemResponse;
import com.kmaleon.model.Item;
import com.kmaleon.model.Location;
import com.kmaleon.model.TransferRequest;
import com.kmaleon.model.Unit;
import com.kmaleon.repository.ItemRepository;
import com.kmaleon.repository.LocationRepository;
import com.kmaleon.repository.TransferRequestRepository;
import com.kmaleon.repository.UnitConversionRepository;
import com.kmaleon.repository.UnitRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferRequestServiceTest {

    @Mock private TransferRequestRepository transferRequestRepository;
    @Mock private UnitConversionRepository unitConversionRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private InventoryMovementService inventoryMovementService;

    @InjectMocks
    private TransferRequestService transferRequestService;

    private Item item;
    private Unit unit;
    private Location branch;
    private TransferRequest pendingRequest;
    private AuthenticatedUser adminCaller;
    private AuthenticatedUser encargadoCaller;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setId(UUID.randomUUID());
        unit.setName("Kilogramo");
        unit.setSymbol("kg");

        item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Arroz");
        item.setBaseUnit(unit);
        item.setActive(true);

        branch = new Location();
        branch.setId(UUID.randomUUID());
        branch.setName("Sucursal Norte");
        branch.setType("branch");
        branch.setActive(true);

        pendingRequest = new TransferRequest();
        pendingRequest.setId(UUID.randomUUID());
        pendingRequest.setItem(item);
        pendingRequest.setUnit(unit);
        pendingRequest.setQuantity(new BigDecimal("10"));
        pendingRequest.setQuantityBase(new BigDecimal("10"));
        pendingRequest.setLocation(branch);
        pendingRequest.setStatus("pending");

        adminCaller = new AuthenticatedUser(UUID.randomUUID(), "inventory_admin", "Admin", null);
        encargadoCaller = new AuthenticatedUser(UUID.randomUUID(), "encargado_sucursal", "Encargado", branch.getId());
    }

    // -------------------------
    // Happy path — findAll
    // -------------------------

    @Test
    void whenFindAll_asAdmin_withNoStatus_thenReturnsAll() {
        when(transferRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(pendingRequest));

        List<TransferRequestItemResponse> result = transferRequestService.findAll(adminCaller, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("pending");
    }

    @Test
    void whenFindAll_asAdmin_withStatusFilter_thenFiltersCorrectly() {
        when(transferRequestRepository.findByStatusOrderByCreatedAtDesc("pending"))
                .thenReturn(List.of(pendingRequest));

        List<TransferRequestItemResponse> result = transferRequestService.findAll(adminCaller, "pending");

        assertThat(result).hasSize(1);
    }

    @Test
    void whenFindAll_asEncargado_thenReturnsOnlyHisBranchRequests() {
        when(transferRequestRepository.findByLocationIdOrderByCreatedAtDesc(branch.getId()))
                .thenReturn(List.of(pendingRequest));

        List<TransferRequestItemResponse> result = transferRequestService.findAll(encargadoCaller, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationId()).isEqualTo(branch.getId());
    }

    @Test
    void whenFindAll_asEncargado_withStatusFilter_thenFiltersCorrectly() {
        when(transferRequestRepository.findByLocationIdAndStatusOrderByCreatedAtDesc(branch.getId(), "pending"))
                .thenReturn(List.of(pendingRequest));

        List<TransferRequestItemResponse> result = transferRequestService.findAll(encargadoCaller, "pending");

        assertThat(result).hasSize(1);
    }

    // -------------------------
    // Happy path — complete / reject
    // -------------------------

    @Test
    void whenComplete_thenStatusChangesToCompleted() {
        Location warehouse = new Location();
        warehouse.setId(UUID.randomUUID());
        warehouse.setType("warehouse");
        warehouse.setActive(true);

        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(locationRepository.findByTypeAndActiveTrue("warehouse")).thenReturn(List.of(warehouse));
        when(transferRequestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        TransferRequestItemResponse result = transferRequestService.complete(pendingRequest.getId());

        assertThat(result.getStatus()).isEqualTo("completed");
    }

    @Test
    void whenReject_thenStatusChangesToRejected() {
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(transferRequestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        TransferRequestItemResponse result = transferRequestService.reject(pendingRequest.getId());

        assertThat(result.getStatus()).isEqualTo("rejected");
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenComplete_andRequestNotFound_thenThrows() {
        UUID unknownId = UUID.randomUUID();
        when(transferRequestRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferRequestService.complete(unknownId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenReject_andRequestNotFound_thenThrows() {
        UUID unknownId = UUID.randomUUID();
        when(transferRequestRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferRequestService.reject(unknownId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenComplete_andAlreadyCompleted_thenThrows() {
        pendingRequest.setStatus("completed");
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> transferRequestService.complete(pendingRequest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenReject_andAlreadyRejected_thenThrows() {
        pendingRequest.setStatus("rejected");
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> transferRequestService.reject(pendingRequest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenFindAll_andEmpty_thenReturnsEmptyList() {
        when(transferRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<TransferRequestItemResponse> result = transferRequestService.findAll(adminCaller, null);

        assertThat(result).isEmpty();
    }
}
