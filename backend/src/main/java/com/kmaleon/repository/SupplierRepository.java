package com.kmaleon.repository;

import com.kmaleon.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    List<Supplier> findByOwnerId(UUID ownerId);

    Optional<Supplier> findByIdAndOwnerId(UUID id, UUID ownerId);
}
