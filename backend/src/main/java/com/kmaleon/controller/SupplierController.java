package com.kmaleon.controller;

import com.kmaleon.dto.SupplierRequest;
import com.kmaleon.dto.SupplierResponse;
import com.kmaleon.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> findAll() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable UUID id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public SupplierResponse deactivate(@PathVariable UUID id) {
        return supplierService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    public SupplierResponse activate(@PathVariable UUID id) {
        return supplierService.activate(id);
    }
}
