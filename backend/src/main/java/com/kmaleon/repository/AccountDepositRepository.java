package com.kmaleon.repository;

import com.kmaleon.model.AccountDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountDepositRepository extends JpaRepository<AccountDeposit, UUID> {

    List<AccountDeposit> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM AccountDeposit d WHERE d.accountId = :accountId")
    Long sumAmountByAccountId(@Param("accountId") UUID accountId);
}
