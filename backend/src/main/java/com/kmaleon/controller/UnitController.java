package com.kmaleon.controller;

import com.kmaleon.dto.UnitRequest;
import com.kmaleon.dto.UnitResponse;
import com.kmaleon.service.UnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    public List<UnitResponse> findAll() {
        return unitService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnitResponse create(@Valid @RequestBody UnitRequest request) {
        return unitService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        unitService.delete(id);
    }
}
