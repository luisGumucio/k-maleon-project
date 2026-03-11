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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDepositRepository depositRepository;
    private final MovementRepository movementRepository;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository,
                          AccountDepositRepository depositRepository,
                          MovementRepository movementRepository,
                          AuditService auditService) {
        this.accountRepository = accountRepository;
        this.depositRepository = depositRepository;
        this.movementRepository = movementRepository;
        this.auditService = auditService;
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
    public AccountDepositResponse deposit(UUID callerId, AccountDepositRequest request) {
        Account account = getOrCreateAccount(callerId);

        AccountDeposit deposit = new AccountDeposit();
        deposit.setAccountId(account.getId());
        deposit.setAmount(request.getAmount());
        deposit.setDescription(request.getDescription());
        deposit.setDate(request.getDate());
        deposit.setCreatedBy(callerId);
        AccountDeposit saved = depositRepository.save(deposit);

        credit(account, request.getAmount());

        auditService.log("ACCOUNT_DEPOSIT", "account_deposits", saved.getId(), saved);

        return AccountDepositResponse.from(saved);
    }

    public List<AccountDepositResponse> getDeposits(UUID callerId) {
        Account account = getOrCreateAccount(callerId);
        return depositRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(AccountDepositResponse::from)
                .toList();
    }

    public AccountSummaryResponse getSummary(UUID callerId) {
        Account account = getOrCreateAccount(callerId);
        Long totalDeposits = depositRepository.sumAmountByAccountId(account.getId());
        Long totalEntradas = movementRepository.sumAmountByOwnerIdAndType(callerId, "entrada");
        Long totalSalidas = movementRepository.sumAmountByOwnerIdAndType(callerId, "salida");
        return AccountSummaryResponse.of(
                account.getId(),
                account.getBalance(),
                totalDeposits,
                totalEntradas,
                totalSalidas,
                account.getUpdatedAt()
        );
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
