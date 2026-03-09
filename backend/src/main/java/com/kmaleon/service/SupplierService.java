package com.kmaleon.service;

import com.kmaleon.dto.SupplierRequest;
import com.kmaleon.dto.SupplierResponse;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> findAll(UUID callerId, String callerRole) {
        List<Supplier> suppliers = "super_admin".equals(callerRole)
                ? supplierRepository.findAll()
                : supplierRepository.findByOwnerId(callerId);
        return suppliers.stream().map(SupplierResponse::from).toList();
    }

    public SupplierResponse findById(UUID id, UUID callerId, String callerRole) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        if (!"super_admin".equals(callerRole) && !callerId.equals(supplier.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este proveedor");
        }
        return SupplierResponse.from(supplier);
    }

    @Transactional
    public SupplierResponse create(UUID callerId, SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setOwnerId(callerId);
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(UUID id, UUID callerId, String callerRole, SupplierRequest request) {
        Supplier supplier = getOwnedSupplier(id, callerId, callerRole);
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse deactivate(UUID id, UUID callerId, String callerRole) {
        Supplier supplier = getOwnedSupplier(id, callerId, callerRole);
        supplier.setActive(false);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse activate(UUID id, UUID callerId, String callerRole) {
        Supplier supplier = getOwnedSupplier(id, callerId, callerRole);
        supplier.setActive(true);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    private Supplier getOwnedSupplier(UUID id, UUID callerId, String callerRole) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        if (!"super_admin".equals(callerRole) && !callerId.equals(supplier.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este proveedor");
        }
        return supplier;
    }
}
