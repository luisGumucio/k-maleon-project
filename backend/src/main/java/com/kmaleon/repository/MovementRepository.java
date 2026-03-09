package com.kmaleon.repository;

import com.kmaleon.model.AccountMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<AccountMovement, UUID> {

    List<AccountMovement> findByOperationIdOrderByDateDesc(UUID operationId);
}
