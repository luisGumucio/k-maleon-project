package com.kmaleon.service;

import com.kmaleon.dto.ShipmentRequest;
import com.kmaleon.dto.ShipmentResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Shipment;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.ShipmentRepository;
import com.kmaleon.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String CALLER_ROLE = "super_admin";

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private Supplier supplier;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");

        shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setOwnerId(CALLER_ID);
        shipment.setSupplier(supplier);
        shipment.setContainerNumber("CARU5170029");
        shipment.setDepartureDate(LocalDate.of(2026, 3, 1));
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void create_withValidSupplier_returnsResponseWithSupplierName() {
        ShipmentRequest request = buildRequest();

        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(shipmentRepository.save(any())).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ShipmentResponse response = shipmentService.create(CALLER_ID, request);

        assertThat(response.getSupplierName()).isEqualTo("Test Supplier");
        assertThat(response.getContainerNumber()).isEqualTo("CARU5170029");
    }

    @Test
    void update_withDocumentUrl_persistsDocumentUrl() {
        String documentUrl = "https://supabase.co/storage/v1/object/public/container-docs/uuid.pdf";
        ShipmentRequest request = buildRequest();
        request.setDocumentUrl(documentUrl);

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        shipmentService.update(shipment.getId(), CALLER_ID, CALLER_ROLE, request);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getDocumentUrl()).isEqualTo(documentUrl);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withSupplierId_returnsFilteredList() {
        when(shipmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(shipment));

        List<ShipmentResponse> responses = shipmentService.findAll(CALLER_ID, CALLER_ROLE, supplier.getId(), null, null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSupplierName()).isEqualTo("Test Supplier");
    }

    @Test
    void delete_withExistingId_deletesShipment() {
        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));

        shipmentService.delete(shipment.getId(), CALLER_ID, CALLER_ROLE);

        verify(shipmentRepository).deleteById(shipment.getId());
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void create_withNonExistentSupplier_throwsResourceNotFoundException() {
        ShipmentRequest request = buildRequest();
        when(supplierRepository.findById(request.getSupplierId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.create(CALLER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void update_withNonExistentShipment_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(shipmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.update(unknownId, CALLER_ID, CALLER_ROLE, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void delete_withNonExistentShipment_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(shipmentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.delete(unknownId, CALLER_ID, CALLER_ROLE))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shipmentRepository, never()).deleteById(any());
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void update_withoutDocumentUrl_doesNotOverwriteExistingDocumentUrl() {
        String existingUrl = "https://supabase.co/storage/v1/object/public/container-docs/existing.pdf";
        shipment.setDocumentUrl(existingUrl);

        ShipmentRequest request = buildRequest();
        request.setDocumentUrl(null);

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        shipmentService.update(shipment.getId(), CALLER_ID, CALLER_ROLE, request);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getDocumentUrl()).isEqualTo(existingUrl);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withNoSupplierId_returnsAllShipments() {
        when(shipmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(shipment));

        List<ShipmentResponse> responses = shipmentService.findAll(CALLER_ID, CALLER_ROLE, null, null, null, null);

        assertThat(responses).hasSize(1);
    }

    private ShipmentRequest buildRequest() {
        ShipmentRequest request = new ShipmentRequest();
        request.setSupplierId(supplier.getId());
        request.setContainerNumber("CARU5170029");
        request.setDepartureDate(LocalDate.of(2026, 3, 1));
        return request;
    }
}
