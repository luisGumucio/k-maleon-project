package com.kmaleon.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountSummaryResponse {

    private UUID id;
    private Long balance;
    private Long totalDeposits;
    private Long totalEntradas;
    private Long totalSalidas;
    private OffsetDateTime updatedAt;

    private AccountSummaryResponse() {}

    public static AccountSummaryResponse of(UUID id, Long balance, Long totalDeposits,
                                            Long totalEntradas, Long totalSalidas, OffsetDateTime updatedAt) {
        AccountSummaryResponse r = new AccountSummaryResponse();
        r.id = id;
        r.balance = balance;
        r.totalDeposits = totalDeposits;
        r.totalEntradas = totalEntradas;
        r.totalSalidas = totalSalidas;
        r.updatedAt = updatedAt;
        return r;
    }

    public UUID getId() { return id; }
    public Long getBalance() { return balance; }
    public Long getTotalDeposits() { return totalDeposits; }
    public Long getTotalEntradas() { return totalEntradas; }
    public Long getTotalSalidas() { return totalSalidas; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
