package com.kmaleon.repository;

import com.kmaleon.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID>, JpaSpecificationExecutor<Operation> {

    List<Operation> findByStatus(String status);

    List<Operation> findBySupplierId(UUID supplierId);

    List<Operation> findByStartDateBetween(LocalDate from, LocalDate to);
}
