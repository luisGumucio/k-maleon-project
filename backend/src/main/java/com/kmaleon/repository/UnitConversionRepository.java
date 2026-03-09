package com.kmaleon.repository;

import com.kmaleon.model.UnitConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitConversionRepository extends JpaRepository<UnitConversion, UUID> {

    List<UnitConversion> findByItemId(UUID itemId);

    boolean existsByFromUnitIdOrToUnitId(UUID fromUnitId, UUID toUnitId);

    boolean existsByItemIdAndFromUnitId(UUID itemId, UUID fromUnitId);
}
