package com.kmaleon.controller;

import com.kmaleon.dto.SupplierRequest;
import com.kmaleon.dto.SupplierResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> findAll(@AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.findAll(caller.getId(), caller.getRole());
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable UUID id,
                                     @AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.findById(id, caller.getId(), caller.getRole());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.create(caller.getId(), request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody SupplierRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.update(id, caller.getId(), caller.getRole(), request);
    }

    @PatchMapping("/{id}/deactivate")
    public SupplierResponse deactivate(@PathVariable UUID id,
                                       @AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.deactivate(id, caller.getId(), caller.getRole());
    }

    @PatchMapping("/{id}/activate")
    public SupplierResponse activate(@PathVariable UUID id,
                                     @AuthenticationPrincipal AuthenticatedUser caller) {
        return supplierService.activate(id, caller.getId(), caller.getRole());
    }
}
