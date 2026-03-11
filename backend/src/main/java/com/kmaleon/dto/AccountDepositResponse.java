package com.kmaleon.dto;

import com.kmaleon.model.AccountDeposit;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountDepositResponse {

    private UUID id;
    private UUID accountId;
    private Long amount;
    private String description;
    private LocalDate date;
    private OffsetDateTime createdAt;

    private AccountDepositResponse() {}

    public static AccountDepositResponse from(AccountDeposit deposit) {
        AccountDepositResponse r = new AccountDepositResponse();
        r.id = deposit.getId();
        r.accountId = deposit.getAccountId();
        r.amount = deposit.getAmount();
        r.description = deposit.getDescription();
        r.date = deposit.getDate();
        r.createdAt = deposit.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public Long getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
