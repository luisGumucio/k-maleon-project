package com.kmaleon.service;

import com.kmaleon.dto.ShipmentDetailResponse;
import com.kmaleon.dto.ShipmentItemRequest;
import com.kmaleon.dto.ShipmentItemResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Shipment;
import com.kmaleon.model.ShipmentItem;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.ShipmentItemRepository;
import com.kmaleon.repository.ShipmentRepository;
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
class ShipmentItemServiceTest {

    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentItemService shipmentItemService;

    private Shipment shipment;
    private ShipmentItem item;

    @BeforeEach
    void setUp() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");

        shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSupplier(supplier);
        shipment.setContainerNumber("CARU5170029");

        item = new ShipmentItem();
        item.setId(UUID.randomUUID());
        item.setShipment(shipment);
        item.setDescription("Converse mujer");
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("5.00"));
        item.setAmount(new BigDecimal("500.00"));
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void findByShipmentId_returnsDetailWithItemsAndTotal() {
        ShipmentItem item2 = new ShipmentItem();
        item2.setId(UUID.randomUUID());
        item2.setShipment(shipment);
        item2.setDescription("Asia mujer");
        item2.setAmount(new BigDecimal("480.00"));

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentItemRepository.findByShipmentIdOrderByCreatedAtAsc(shipment.getId()))
                .thenReturn(List.of(item, item2));

        ShipmentDetailResponse detail = shipmentItemService.findByShipmentId(shipment.getId());

        assertThat(detail.getItems()).hasSize(2);
        assertThat(detail.getTotalAmount()).isEqualByComparingTo(new BigDecimal("980.00"));
        assertThat(detail.getContainerNumber()).isEqualTo("CARU5170029");
        assertThat(detail.getSupplierName()).isEqualTo("Test Supplier");
    }

    @Test
    void create_withValidShipment_returnsResponse() {
        ShipmentItemRequest request = buildRequest();

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentItemRepository.save(any())).thenAnswer(inv -> {
            ShipmentItem i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        ShipmentItemResponse response = shipmentItemService.create(request);

        assertThat(response.getDescription()).isEqualTo("Converse mujer");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getContainerNumber()).isEqualTo("CARU5170029");
    }

    @Test
    void update_withValidItem_updatesFields() {
        ShipmentItemRequest request = buildRequest();
        request.setDescription("Converse mujer actualizado");
        request.setAmount(new BigDecimal("600.00"));

        when(shipmentItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(shipmentItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShipmentItemResponse response = shipmentItemService.update(item.getId(), request);

        assertThat(response.getDescription()).isEqualTo("Converse mujer actualizado");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    void delete_withExistingItem_deletesSuccessfully() {
        when(shipmentItemRepository.existsById(item.getId())).thenReturn(true);

        shipmentItemService.delete(item.getId());

        verify(shipmentItemRepository).deleteById(item.getId());
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void findByShipmentId_withNonExistentShipment_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(shipmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentItemService.findByShipmentId(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withNonExistentShipment_throwsResourceNotFoundException() {
        ShipmentItemRequest request = buildRequest();
        when(shipmentRepository.findById(request.getShipmentId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentItemService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentItemRepository, never()).save(any());
    }

    @Test
    void update_withNonExistentItem_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(shipmentItemRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentItemService.update(unknownId, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentItemRepository, never()).save(any());
    }

    @Test
    void delete_withNonExistentItem_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(shipmentItemRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> shipmentItemService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentItemRepository, never()).deleteById(any());
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void findByShipmentId_withNoItems_returnsEmptyListAndZeroTotal() {
        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentItemRepository.findByShipmentIdOrderByCreatedAtAsc(shipment.getId()))
                .thenReturn(List.of());

        ShipmentDetailResponse detail = shipmentItemService.findByShipmentId(shipment.getId());

        assertThat(detail.getItems()).isEmpty();
        assertThat(detail.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void findByShipmentId_totalAmountIsSumOfAllItemAmounts() {
        ShipmentItem item2 = new ShipmentItem();
        item2.setId(UUID.randomUUID());
        item2.setShipment(shipment);
        item2.setDescription("Sandalia mujer");
        item2.setAmount(new BigDecimal("200.00"));

        ShipmentItem item3 = new ShipmentItem();
        item3.setId(UUID.randomUUID());
        item3.setShipment(shipment);
        item3.setDescription("Asia mujer");
        item3.setAmount(new BigDecimal("480.00"));

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentItemRepository.findByShipmentIdOrderByCreatedAtAsc(shipment.getId()))
                .thenReturn(List.of(item, item2, item3));

        ShipmentDetailResponse detail = shipmentItemService.findByShipmentId(shipment.getId());

        // 500 + 200 + 480 = 1180
        assertThat(detail.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1180.00"));
    }

    private ShipmentItemRequest buildRequest() {
        ShipmentItemRequest request = new ShipmentItemRequest();
        request.setShipmentId(shipment.getId());
        request.setDescription("Converse mujer");
        request.setQuantity(100);
        request.setUnitPrice(new BigDecimal("5.00"));
        request.setAmount(new BigDecimal("500.00"));
        return request;
    }
}
