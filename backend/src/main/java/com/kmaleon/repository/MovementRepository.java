package com.kmaleon.repository;

import com.kmaleon.model.AccountMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<AccountMovement, UUID> {

    List<AccountMovement> findByOperationIdOrderByDateDesc(UUID operationId);

    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM AccountMovement m JOIN m.operation o WHERE o.ownerId = :ownerId AND m.type = :type")
    Long sumAmountByOwnerIdAndType(@Param("ownerId") UUID ownerId, @Param("type") String type);
}
