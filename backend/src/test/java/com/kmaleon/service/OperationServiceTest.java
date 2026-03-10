package com.kmaleon.service;

import com.kmaleon.dto.OperationRequest;
import com.kmaleon.dto.OperationSummaryResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Operation;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.OperationRepository;
import com.kmaleon.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String CALLER_ROLE = "super_admin";

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private OperationService operationService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenCreate_thenReturnsSummaryWithCorrectFields() {
        OperationRequest request = buildRequest(1000000L);

        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(operationRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        OperationSummaryResponse response = operationService.create(CALLER_ID, request);

        assertThat(response.getTotalAmount()).isEqualTo(1000000L);
        assertThat(response.getPaidAmount()).isEqualTo(0L);
        assertThat(response.getPendingAmount()).isEqualTo(1000000L);
        assertThat(response.getSupplierName()).isEqualTo("Test Supplier");
        assertThat(response.getContainer()).isEqualTo("CARU5170029");
    }

    @Test
    void whenUpdate_thenChangesFieldsAndReturnsSummary() {
        Operation existing = buildOperation(1000000L, 0L);
        OperationRequest request = buildRequest(1500000L);
        request.setStatus("completed");

        when(operationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(operationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OperationSummaryResponse response = operationService.update(existing.getId(), CALLER_ID, CALLER_ROLE, request);

        assertThat(response.getTotalAmount()).isEqualTo(1500000L);
        assertThat(response.getStatus()).isEqualTo("completed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenFindAll_thenReturnsMappedList() {
        Operation op = buildOperation(1000000L, 0L);
        when(operationRepository.findAll(any(Specification.class))).thenReturn(List.of(op));

        List<OperationSummaryResponse> responses = operationService.findAll(CALLER_ID, CALLER_ROLE, null, null, null, null);

        assertThat(responses).hasSize(1);
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenFindById_andNotFound_thenThrowsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(operationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationService.findById(id, CALLER_ID, CALLER_ROLE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void whenCreate_andSupplierNotFound_thenThrowsResourceNotFoundException() {
        OperationRequest request = buildRequest(1000000L);
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationService.create(CALLER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void whenUpdate_andOperationNotFound_thenThrowsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(operationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationService.update(id, CALLER_ID, CALLER_ROLE, buildRequest(1000000L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenFindById_andPartiallyPaid_thenPendingAmountIsCorrect() {
        Operation op = buildOperation(1000000L, 600000L);
        when(operationRepository.findById(op.getId())).thenReturn(Optional.of(op));

        OperationSummaryResponse response = operationService.findById(op.getId(), CALLER_ID, CALLER_ROLE);

        assertThat(response.getPaidAmount()).isEqualTo(600000L);
        assertThat(response.getPendingAmount()).isEqualTo(400000L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenFindAll_andEmpty_thenReturnsEmptyList() {
        when(operationRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<OperationSummaryResponse> responses = operationService.findAll(CALLER_ID, CALLER_ROLE, null, null, null, null);

        assertThat(responses).isEmpty();
    }

    @Test
    void whenFindById_andFullyPaid_thenPendingAmountIsZero() {
        Operation op = buildOperation(1000000L, 1000000L);
        when(operationRepository.findById(op.getId())).thenReturn(Optional.of(op));

        OperationSummaryResponse response = operationService.findById(op.getId(), CALLER_ID, CALLER_ROLE);

        assertThat(response.getPendingAmount()).isEqualTo(0L);
    }

    private OperationRequest buildRequest(Long totalAmount) {
        OperationRequest request = new OperationRequest();
        request.setSupplierId(supplier.getId());
        request.setContainer("CARU5170029");
        request.setTotalAmount(totalAmount);
        request.setStartDate(LocalDate.now());
        return request;
    }

    private Operation buildOperation(Long totalAmount, Long paidAmount) {
        Operation op = new Operation();
        op.setId(UUID.randomUUID());
        op.setOwnerId(CALLER_ID);
        op.setSupplier(supplier);
        op.setContainer("CARU5170029");
        op.setTotalAmount(totalAmount);
        op.setPaidAmount(paidAmount);
        op.setStartDate(LocalDate.now());
        op.setStatus("active");
        return op;
    }

    private Operation withId(Operation op) {
        op.setId(UUID.randomUUID());
        return op;
    }
}
