package com.kmaleon.service;

import com.kmaleon.dto.UnitRequest;
import com.kmaleon.dto.UnitResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Unit;
import com.kmaleon.repository.UnitConversionRepository;
import com.kmaleon.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitConversionRepository unitConversionRepository;

    public UnitService(UnitRepository unitRepository,
                       UnitConversionRepository unitConversionRepository) {
        this.unitRepository = unitRepository;
        this.unitConversionRepository = unitConversionRepository;
    }

    public List<UnitResponse> findAll() {
        return unitRepository.findAll().stream()
                .map(UnitResponse::from)
                .toList();
    }

    @Transactional
    public UnitResponse create(UnitRequest request) {
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setSymbol(request.getSymbol());
        return UnitResponse.from(unitRepository.save(unit));
    }

    @Transactional
    public void delete(UUID id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));

        if (unitConversionRepository.existsByFromUnitIdOrToUnitId(id, id)) {
            throw new IllegalArgumentException("Cannot delete unit: it has associated conversions");
        }

        unitRepository.delete(unit);
    }
}
