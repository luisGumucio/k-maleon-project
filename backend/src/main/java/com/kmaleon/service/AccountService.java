package com.kmaleon.service;

import com.kmaleon.dto.AccountBalanceResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Account;
import com.kmaleon.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountBalanceResponse getBalance() {
        return AccountBalanceResponse.from(getAccount());
    }

    @Transactional
    public AccountBalanceResponse setInitialBalance(Long amount) {
        Account account = accountRepository.findAll().stream()
                .findFirst()
                .orElse(new Account());
        account.setBalance(amount);
        return AccountBalanceResponse.from(accountRepository.save(account));
    }

    void credit(Account account, Long amount) {
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    void debit(Account account, Long amount) {
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    Account getAccount() {
        return accountRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No account found. Set initial balance first."));
    }
}
