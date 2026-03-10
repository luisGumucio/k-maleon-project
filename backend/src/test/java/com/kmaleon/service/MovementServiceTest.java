package com.kmaleon.service;

import com.kmaleon.dto.MovementRequest;
import com.kmaleon.dto.MovementResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Account;
import com.kmaleon.model.AccountMovement;
import com.kmaleon.model.Operation;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.MovementRepository;
import com.kmaleon.repository.OperationRepository;
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
class MovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private AuditService auditService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MovementService movementService;

    private Operation operation;
    private Account account;

    @BeforeEach
    void setUp() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");

        operation = new Operation();
        operation.setId(UUID.randomUUID());
        operation.setSupplier(supplier);
        operation.setContainer("CARU5170029");
        operation.setTotalAmount(1000000L);
        operation.setPaidAmount(0L);

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setBalance(500000L);
    }

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenSalida_thenUpdatesPaidAmountAndDebitsBalance() {
        MovementRequest request = buildRequest("salida", 200000L);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        MovementResponse response = movementService.registerMovement(request);

        ArgumentCaptor<Operation> captor = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).save(captor.capture());
        assertThat(captor.getValue().getPaidAmount()).isEqualTo(200000L);

        verify(accountService).debit(account, 200000L);
        verify(accountService, never()).credit(any(), any());
        assertThat(response.getType()).isEqualTo("salida");
    }

    @Test
    void whenEntrada_thenOnlyCreditsBalance_doesNotChangePaidAmount() {
        MovementRequest request = buildRequest("entrada", 300000L);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        MovementResponse response = movementService.registerMovement(request);

        verify(operationRepository, never()).save(any());
        verify(accountService).credit(account, 300000L);
        verify(accountService, never()).debit(any(), any());
        assertThat(operation.getPaidAmount()).isEqualTo(0L);
        assertThat(response.getType()).isEqualTo("entrada");
    }

    @Test
    void whenSalida_thenAuditLogIsCreated() {
        MovementRequest request = buildRequest("salida", 100000L);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        movementService.registerMovement(request);

        verify(auditService).log(eq("movement_created"), eq("account_movements"), any(), eq(request));
    }

    @Test
    void whenFindByOperationId_thenReturnsMappedList() {
        AccountMovement m = buildSavedMovement("salida", 100000L);
        when(movementRepository.findByOperationIdOrderByDateDesc(operation.getId())).thenReturn(List.of(m));

        List<MovementResponse> responses = movementService.findByOperationId(operation.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getType()).isEqualTo("salida");
    }

    @Test
    void whenSalidaWithAttachment_succeeds_thenStorageDeleteIsNeverCalled() {
        MovementRequest request = buildRequest("salida", 200000L);
        request.setAttachmentUrl("https://supabase.co/storage/v1/object/public/financial-docs/uuid.pdf");

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        movementService.registerMovement(request);

        verify(storageService, never()).delete(any(), any());
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenOperationNotFound_thenThrowsAndNothingIsPersisted() {
        MovementRequest request = buildRequest("salida", 100000L);
        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(movementRepository, never()).save(any());
        verify(accountService, never()).debit(any(), any());
    }

    @Test
    void whenAccountNotFound_thenThrowsAndMovementIsNotPersisted() {
        MovementRequest request = buildRequest("salida", 100000L);
        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenThrow(new ResourceNotFoundException("No account found"));

        assertThatThrownBy(() -> movementService.registerMovement(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(movementRepository, never()).save(any());
    }

    @Test
    void whenRegistrationFails_andAttachmentUrlPresent_thenDeleteIsCalledOnOrphan() {
        String attachmentUrl = "https://supabase.co/storage/v1/object/public/financial-docs/uuid.pdf";
        MovementRequest request = buildRequest("salida", 100000L);
        request.setAttachmentUrl(attachmentUrl);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storageService).delete(attachmentUrl, StorageService.BUCKET_FINANCIAL);
    }

    @Test
    void whenRegistrationFails_andNoAttachmentUrl_thenDeleteIsNeverCalled() {
        MovementRequest request = buildRequest("salida", 100000L);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storageService, never()).delete(any(), any());
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenCurrencyIsNull_thenDefaultsToUSD() {
        MovementRequest request = buildRequest("entrada", 100000L);
        request.setCurrency(null);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        MovementResponse response = movementService.registerMovement(request);

        assertThat(response.getCurrency()).isEqualTo("USD");
    }

    @Test
    void whenMultipleSalidas_thenPaidAmountAccumulates() {
        operation.setPaidAmount(300000L);
        MovementRequest request = buildRequest("salida", 200000L);

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.of(operation));
        when(accountService.getAccount(any())).thenReturn(account);
        when(movementRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0)));

        movementService.registerMovement(request);

        ArgumentCaptor<Operation> captor = ArgumentCaptor.forClass(Operation.class);
        verify(operationRepository).save(captor.capture());
        assertThat(captor.getValue().getPaidAmount()).isEqualTo(500000L);
    }

    @Test
    void whenFindByOperationId_andNoMovements_thenReturnsEmptyList() {
        when(movementRepository.findByOperationIdOrderByDateDesc(operation.getId())).thenReturn(List.of());

        List<MovementResponse> responses = movementService.findByOperationId(operation.getId());

        assertThat(responses).isEmpty();
    }

    @Test
    void whenRegistrationFails_andAttachmentUrlIsBlank_thenDeleteIsNeverCalled() {
        MovementRequest request = buildRequest("salida", 100000L);
        request.setAttachmentUrl("   ");

        when(operationRepository.findById(request.getOperationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerMovement(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storageService, never()).delete(any(), any());
    }

    private MovementRequest buildRequest(String type, Long amount) {
        MovementRequest request = new MovementRequest();
        request.setOperationId(operation.getId());
        request.setType(type);
        request.setAmount(amount);
        request.setDate(LocalDate.now());
        return request;
    }

    private AccountMovement withId(AccountMovement movement) {
        movement.setId(UUID.randomUUID());
        return movement;
    }

    private AccountMovement buildSavedMovement(String type, Long amount) {
        AccountMovement m = new AccountMovement();
        m.setId(UUID.randomUUID());
        m.setOperation(operation);
        m.setType(type);
        m.setAmount(amount);
        m.setCurrency("USD");
        m.setDate(LocalDate.now());
        return m;
    }
}
