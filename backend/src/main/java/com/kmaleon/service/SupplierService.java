package com.kmaleon.service;

import com.kmaleon.dto.SupplierRequest;
import com.kmaleon.dto.SupplierResponse;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }
}
