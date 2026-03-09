package com.kmaleon.service;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Account;
import com.kmaleon.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenGetBalance_thenReturnsCurrentBalance() {
        Account account = accountWithBalance(750000L);
        when(accountRepository.findAll()).thenReturn(List.of(account));

        AccountBalanceResponse response = accountService.getBalance();

        assertThat(response.getBalance()).isEqualTo(750000L);
    }

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
    void whenSetInitialBalance_andNoAccountExists_thenCreatesNewAccount() {
        when(accountRepository.findAll()).thenReturn(List.of());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountBalanceResponse response = accountService.setInitialBalance(1000000L);

        assertThat(response.getBalance()).isEqualTo(1000000L);
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenGetAccount_andNoneExists_thenThrowsResourceNotFoundException() {
        when(accountRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> accountService.getAccount())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenSetInitialBalance_andAccountAlreadyExists_thenUpdatesExistingAccount() {
        Account existing = accountWithBalance(500000L);
        when(accountRepository.findAll()).thenReturn(List.of(existing));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountBalanceResponse response = accountService.setInitialBalance(2000000L);

        assertThat(response.getBalance()).isEqualTo(2000000L);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
    }

    @Test
    void whenDebit_andAmountExceedsBalance_thenBalanceGoesNegative() {
        Account account = accountWithBalance(100000L);

        accountService.debit(account, 300000L);

        assertThat(account.getBalance()).isEqualTo(-200000L);
        verify(accountRepository).save(account);
    }

    @Test
    void whenCredit_thenBalanceIdIsPreserved() {
        UUID originalId = UUID.randomUUID();
        Account account = accountWithBalance(100000L);
        account.setId(originalId);

        accountService.credit(account, 50000L);

        assertThat(account.getId()).isEqualTo(originalId);
    }

    private Account accountWithBalance(Long balance) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setBalance(balance);
        return account;
    }
}
