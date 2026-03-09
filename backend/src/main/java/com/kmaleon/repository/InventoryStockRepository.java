package com.kmaleon.repository;

import com.kmaleon.model.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {

    List<InventoryStock> findByItemId(UUID itemId);

    Optional<InventoryStock> findByItemIdAndLocationId(UUID itemId, UUID locationId);
}
