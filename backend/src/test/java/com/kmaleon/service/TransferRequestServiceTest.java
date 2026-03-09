package com.kmaleon.service;

import com.kmaleon.dto.RejectRequestDto;
import com.kmaleon.dto.TransferRequestCreateDto;
import com.kmaleon.dto.TransferRequestDto;
import com.kmaleon.dto.TransferRequestResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.*;
import com.kmaleon.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferRequestServiceTest {

    @Mock private TransferRequestRepository transferRequestRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private UnitConversionRepository conversionRepository;
    @Mock private InventoryMovementService inventoryMovementService;

    @InjectMocks
    private TransferRequestService service;

    private Unit baseUnit;
    private Item item;
    private Location branch;
    private TransferRequest pendingRequest;

    @BeforeEach
    void setUp() {
        baseUnit = new Unit();
        baseUnit.setId(UUID.randomUUID());
        baseUnit.setName("gramo");
        baseUnit.setSymbol("g");

        item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Arroz");
        item.setBaseUnit(baseUnit);
        item.setActive(true);

        branch = new Location();
        branch.setId(UUID.randomUUID());
        branch.setName("Sucursal Norte");
        branch.setType("branch");
        branch.setActive(true);

        pendingRequest = new TransferRequest();
        pendingRequest.setId(UUID.randomUUID());
        pendingRequest.setItem(item);
        pendingRequest.setUnit(baseUnit);
        pendingRequest.setQuantity(new BigDecimal("10"));
        pendingRequest.setQuantityBase(new BigDecimal("10"));
        pendingRequest.setLocation(branch);
        pendingRequest.setStatus("pending");
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenCreate_thenSavesWithPendingStatus() {
        TransferRequestCreateDto dto = buildCreateDto(item.getId(), baseUnit.getId(), "10", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(baseUnit.getId())).thenReturn(Optional.of(baseUnit));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(transferRequestRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        TransferRequestResponse response = service.create(dto);

        assertThat(response.getStatus()).isEqualTo("pending");
        assertThat(response.getQuantityBase()).isEqualByComparingTo("10");
    }

    @Test
    void whenCreate_withAltUnit_thenConvertsToBase() {
        Unit vaso = new Unit();
        vaso.setId(UUID.randomUUID());
        vaso.setName("vaso");
        vaso.setSymbol("vaso");

        UnitConversion conversion = new UnitConversion();
        conversion.setId(UUID.randomUUID());
        conversion.setItem(item);
        conversion.setFromUnit(vaso);
        conversion.setToUnit(baseUnit);
        conversion.setFactor(new BigDecimal("200"));

        TransferRequestCreateDto dto = buildCreateDto(item.getId(), vaso.getId(), "3", branch.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(unitRepository.findById(vaso.getId())).thenReturn(Optional.of(vaso));
        when(locationRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(conversionRepository.findByItemId(item.getId())).thenReturn(List.of(conversion));
        when(transferRequestRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        TransferRequestResponse response = service.create(dto);

        // 3 vasos × 200 g/vaso = 600 g
        assertThat(response.getQuantityBase()).isEqualByComparingTo("600");
    }

    @Test
    void whenComplete_thenExecutesTransferAndMarksCompleted() {
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(inventoryMovementService.transfer(any())).thenReturn(null);
        when(transferRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequestResponse response = service.complete(pendingRequest.getId());

        verify(inventoryMovementService).transfer(any(TransferRequestDto.class));
        assertThat(response.getStatus()).isEqualTo("completed");
    }

    @Test
    void whenReject_thenMarksRejectedWithoutExecutingTransfer() {
        RejectRequestDto rejectDto = new RejectRequestDto();
        rejectDto.setNotes("Sin stock disponible");

        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(transferRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequestResponse response = service.reject(pendingRequest.getId(), rejectDto);

        verify(inventoryMovementService, never()).transfer(any());
        assertThat(response.getStatus()).isEqualTo("rejected");
    }

    @Test
    void whenReject_withNotes_thenNotesAreUpdated() {
        RejectRequestDto rejectDto = new RejectRequestDto();
        rejectDto.setNotes("No hay stock");

        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(transferRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.reject(pendingRequest.getId(), rejectDto);

        assertThat(pendingRequest.getNotes()).isEqualTo("No hay stock");
    }

    @Test
    void whenFindPending_thenReturnsMappedList() {
        when(transferRequestRepository.findByStatusOrderByCreatedAtDesc("pending"))
                .thenReturn(List.of(pendingRequest));

        List<TransferRequestResponse> result = service.findPending();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("pending");
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenCreate_andItemNotFound_thenThrows() {
        TransferRequestCreateDto dto = buildCreateDto(UUID.randomUUID(), baseUnit.getId(), "10", branch.getId());
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transferRequestRepository, never()).save(any());
    }

    @Test
    void whenCreate_andItemInactive_thenThrows() {
        item.setActive(false);
        TransferRequestCreateDto dto = buildCreateDto(item.getId(), baseUnit.getId(), "10", branch.getId());
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void whenComplete_andRequestNotFound_thenThrows() {
        UUID id = UUID.randomUUID();
        when(transferRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryMovementService, never()).transfer(any());
    }

    @Test
    void whenComplete_andRequestAlreadyCompleted_thenThrows() {
        pendingRequest.setStatus("completed");
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> service.complete(pendingRequest.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already completed");

        verify(inventoryMovementService, never()).transfer(any());
    }

    @Test
    void whenReject_andRequestAlreadyRejected_thenThrows() {
        pendingRequest.setStatus("rejected");
        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> service.reject(pendingRequest.getId(), new RejectRequestDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already rejected");
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenFindPending_andNoneExist_thenReturnsEmptyList() {
        when(transferRequestRepository.findByStatusOrderByCreatedAtDesc("pending")).thenReturn(List.of());

        List<TransferRequestResponse> result = service.findPending();

        assertThat(result).isEmpty();
    }

    @Test
    void whenReject_withNullNotes_thenNotesAreNotOverwritten() {
        pendingRequest.setNotes("Nota original");
        RejectRequestDto rejectDto = new RejectRequestDto();
        rejectDto.setNotes(null);

        when(transferRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(transferRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.reject(pendingRequest.getId(), rejectDto);

        assertThat(pendingRequest.getNotes()).isEqualTo("Nota original");
    }

    // -------------------------
    // Helpers
    // -------------------------

    private TransferRequestCreateDto buildCreateDto(UUID itemId, UUID unitId, String quantity, UUID locationId) {
        TransferRequestCreateDto dto = new TransferRequestCreateDto();
        dto.setItemId(itemId);
        dto.setUnitId(unitId);
        dto.setQuantity(new BigDecimal(quantity));
        dto.setLocationId(locationId);
        return dto;
    }

    private TransferRequest withId(TransferRequest r) {
        r.setId(UUID.randomUUID());
        return r;
    }
}
