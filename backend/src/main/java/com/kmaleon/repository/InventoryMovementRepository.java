package com.kmaleon.repository;

import com.kmaleon.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID>,
        JpaSpecificationExecutor<InventoryMovement> {
}
