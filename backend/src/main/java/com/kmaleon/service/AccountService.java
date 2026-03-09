package com.kmaleon.service;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.model.Account;
import com.kmaleon.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountBalanceResponse getBalance(UUID callerId) {
        return AccountBalanceResponse.from(getOrCreateAccount(callerId));
    }

    @Transactional
    public AccountBalanceResponse setInitialBalance(UUID callerId, Long amount) {
        Account account = getOrCreateAccount(callerId);
        account.setBalance(amount);
        return AccountBalanceResponse.from(accountRepository.save(account));
    }

    @Transactional
    void credit(Account account, Long amount) {
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    @Transactional
    void debit(Account account, Long amount) {
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    Account getAccount(UUID callerId) {
        return getOrCreateAccount(callerId);
    }

    private Account getOrCreateAccount(UUID ownerId) {
        return accountRepository.findByOwnerId(ownerId).orElseGet(() -> {
            Account account = new Account();
            account.setOwnerId(ownerId);
            account.setBalance(0L);
            return accountRepository.save(account);
        });
    }
}
