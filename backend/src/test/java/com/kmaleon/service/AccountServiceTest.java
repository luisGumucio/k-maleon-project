package com.kmaleon.service;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.dto.AccountDepositRequest;
import com.kmaleon.dto.AccountDepositResponse;
import com.kmaleon.dto.AccountSummaryResponse;
import com.kmaleon.model.Account;
import com.kmaleon.model.AccountDeposit;
import com.kmaleon.repository.AccountDepositRepository;
import com.kmaleon.repository.AccountRepository;
import com.kmaleon.repository.MovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final UUID CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock private AccountRepository accountRepository;
    @Mock private AccountDepositRepository depositRepository;
    @Mock private MovementRepository movementRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    // -------------------------
    // getBalance
    // -------------------------

    @Test
    void whenGetBalance_thenReturnsCurrentBalance() {
        Account account = accountWithBalance(750000L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));

        AccountBalanceResponse response = accountService.getBalance(CALLER_ID);

        assertThat(response.getBalance()).isEqualTo(750000L);
    }

    // -------------------------
    // credit / debit
    // -------------------------

    @Test
    void whenCredit_thenAddsAmountToBalance() {
        Account account = accountWithBalance(500000L);

        accountService.credit(account, 200000L);

        assertThat(account.getBalance()).isEqualTo(700000L);
        verify(accountRepository).save(account);
    }

    @Test
    void whenDebit_thenSubtractsAmountFromBalance() {
        Account account = accountWithBalance(500000L);

        accountService.debit(account, 150000L);

        assertThat(account.getBalance()).isEqualTo(350000L);
        verify(accountRepository).save(account);
    }

    @Test
    void whenDebit_andAmountExceedsBalance_thenBalanceGoesNegative() {
        Account account = accountWithBalance(100000L);

        accountService.debit(account, 300000L);

        assertThat(account.getBalance()).isEqualTo(-200000L);
        verify(accountRepository).save(account);
    }

    @Test
    void whenCredit_thenAccountIdIsPreserved() {
        UUID originalId = UUID.randomUUID();
        Account account = accountWithBalance(100000L);
        account.setId(originalId);

        accountService.credit(account, 50000L);

        assertThat(account.getId()).isEqualTo(originalId);
    }

    // -------------------------
    // setInitialBalance
    // -------------------------

    @Test
    void whenSetInitialBalance_andNoAccountExists_thenCreatesNewAccount() {
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountBalanceResponse response = accountService.setInitialBalance(CALLER_ID, 1000000L);

        assertThat(response.getBalance()).isEqualTo(1000000L);
    }

    @Test
    void whenSetInitialBalance_andAccountAlreadyExists_thenUpdatesExistingAccount() {
        Account existing = accountWithBalance(500000L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountBalanceResponse response = accountService.setInitialBalance(CALLER_ID, 2000000L);

        assertThat(response.getBalance()).isEqualTo(2000000L);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
    }

    // -------------------------
    // getAccount
    // -------------------------

    @Test
    void whenGetAccount_andNoneExists_thenReturnsNewAccountWithZeroBalance() {
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.getAccount(CALLER_ID);

        assertThat(account.getBalance()).isEqualTo(0L);
        assertThat(account.getOwnerId()).isEqualTo(CALLER_ID);
    }

    // -------------------------
    // deposit
    // -------------------------

    @Test
    void whenDeposit_thenBalanceIncrementsAndDepositIsSaved() {
        Account account = accountWithBalance(500000L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));
        when(depositRepository.save(any())).thenAnswer(inv -> {
            AccountDeposit d = inv.getArgument(0);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountDepositRequest request = new AccountDepositRequest();
        request.setAmount(200000L);
        request.setDate(LocalDate.now());
        request.setDescription("Fondeo marzo");

        AccountDepositResponse response = accountService.deposit(CALLER_ID, request);

        assertThat(account.getBalance()).isEqualTo(700000L);
        assertThat(response.getAmount()).isEqualTo(200000L);
        assertThat(response.getDescription()).isEqualTo("Fondeo marzo");

        verify(depositRepository).save(any(AccountDeposit.class));
        verify(accountRepository).save(account);
        verify(auditService).log(eq("ACCOUNT_DEPOSIT"), eq("account_deposits"), any(), any());
    }

    @Test
    void whenDeposit_thenCreatedByIsSetToCaller() {
        Account account = accountWithBalance(0L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));
        when(depositRepository.save(any())).thenAnswer(inv -> {
            AccountDeposit d = inv.getArgument(0);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountDepositRequest request = new AccountDepositRequest();
        request.setAmount(100000L);
        request.setDate(LocalDate.now());

        accountService.deposit(CALLER_ID, request);

        ArgumentCaptor<AccountDeposit> captor = ArgumentCaptor.forClass(AccountDeposit.class);
        verify(depositRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(CALLER_ID);
    }

    @Test
    void whenDeposit_andAccountDoesNotExist_thenCreatesAccountAndDeposits() {
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(depositRepository.save(any())).thenAnswer(inv -> {
            AccountDeposit d = inv.getArgument(0);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });

        AccountDepositRequest request = new AccountDepositRequest();
        request.setAmount(300000L);
        request.setDate(LocalDate.now());

        AccountDepositResponse response = accountService.deposit(CALLER_ID, request);

        assertThat(response.getAmount()).isEqualTo(300000L);
    }

    // -------------------------
    // getDeposits
    // -------------------------

    @Test
    void whenGetDeposits_thenReturnsListOrderedByRepo() {
        Account account = accountWithBalance(0L);
        UUID accountId = account.getId();
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));

        AccountDeposit d1 = depositOf(accountId, 100000L, "Enero");
        AccountDeposit d2 = depositOf(accountId, 200000L, "Febrero");
        when(depositRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(d2, d1));

        List<AccountDepositResponse> result = accountService.getDeposits(CALLER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isEqualTo(200000L);
        assertThat(result.get(1).getAmount()).isEqualTo(100000L);
    }

    @Test
    void whenGetDeposits_andNoneExist_thenReturnsEmptyList() {
        Account account = accountWithBalance(0L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));
        when(depositRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()))
                .thenReturn(List.of());

        List<AccountDepositResponse> result = accountService.getDeposits(CALLER_ID);

        assertThat(result).isEmpty();
    }

    // -------------------------
    // getSummary
    // -------------------------

    @Test
    void whenGetSummary_thenReturnsCombinedTotals() {
        Account account = accountWithBalance(800000L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));
        when(depositRepository.sumAmountByAccountId(account.getId())).thenReturn(1000000L);
        when(movementRepository.sumAmountByOwnerIdAndType(CALLER_ID, "entrada")).thenReturn(300000L);
        when(movementRepository.sumAmountByOwnerIdAndType(CALLER_ID, "salida")).thenReturn(500000L);

        AccountSummaryResponse summary = accountService.getSummary(CALLER_ID);

        assertThat(summary.getBalance()).isEqualTo(800000L);
        assertThat(summary.getTotalDeposits()).isEqualTo(1000000L);
        assertThat(summary.getTotalEntradas()).isEqualTo(300000L);
        assertThat(summary.getTotalSalidas()).isEqualTo(500000L);
    }

    @Test
    void whenGetSummary_andNoMovementsOrDeposits_thenAllTotalsAreZero() {
        Account account = accountWithBalance(0L);
        when(accountRepository.findByOwnerId(CALLER_ID)).thenReturn(Optional.of(account));
        when(depositRepository.sumAmountByAccountId(account.getId())).thenReturn(0L);
        when(movementRepository.sumAmountByOwnerIdAndType(CALLER_ID, "entrada")).thenReturn(0L);
        when(movementRepository.sumAmountByOwnerIdAndType(CALLER_ID, "salida")).thenReturn(0L);

        AccountSummaryResponse summary = accountService.getSummary(CALLER_ID);

        assertThat(summary.getTotalDeposits()).isZero();
        assertThat(summary.getTotalEntradas()).isZero();
        assertThat(summary.getTotalSalidas()).isZero();
    }

    // -------------------------
    // Helpers
    // -------------------------

    private Account accountWithBalance(Long balance) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setOwnerId(CALLER_ID);
        account.setBalance(balance);
        return account;
    }

    private AccountDeposit depositOf(UUID accountId, Long amount, String description) {
        AccountDeposit d = new AccountDeposit();
        d.setAccountId(accountId);
        d.setAmount(amount);
        d.setDescription(description);
        d.setDate(LocalDate.now());
        d.setCreatedAt(OffsetDateTime.now());
        return d;
    }
}
