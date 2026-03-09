package com.kmaleon.dto;

import com.kmaleon.model.Account;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountBalanceResponse {

    private UUID id;
    private Long balance;
    private OffsetDateTime updatedAt;

    private AccountBalanceResponse() {}

    public static AccountBalanceResponse from(Account account) {
        return new Builder()
                .id(account.getId())
                .balance(account.getBalance())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AccountBalanceResponse response = new AccountBalanceResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder balance(Long balance) { response.balance = balance; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { response.updatedAt = updatedAt; return this; }

        public AccountBalanceResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public Long getBalance() { return balance; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
