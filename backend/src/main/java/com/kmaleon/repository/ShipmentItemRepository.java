package com.kmaleon.repository;

import com.kmaleon.model.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, UUID> {

    List<ShipmentItem> findByShipmentIdOrderByCreatedAtAsc(UUID shipmentId);
}
