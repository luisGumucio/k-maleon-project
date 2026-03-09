package com.kmaleon.service;

import com.kmaleon.dto.OperationRequest;
import com.kmaleon.dto.OperationSummaryResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Operation;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.OperationRepository;
import com.kmaleon.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OperationService {

    private final OperationRepository operationRepository;
    private final SupplierRepository supplierRepository;

    public OperationService(OperationRepository operationRepository, SupplierRepository supplierRepository) {
        this.operationRepository = operationRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<OperationSummaryResponse> findAll(String status, UUID supplierId, LocalDate from, LocalDate to) {
        return operationRepository.findAll(buildSpec(status, supplierId, from, to)).stream()
                .map(OperationSummaryResponse::from)
                .toList();
    }

    public OperationSummaryResponse findById(UUID id) {
        return operationRepository.findById(id)
                .map(OperationSummaryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found: " + id));
    }

    @Transactional
    public OperationSummaryResponse create(OperationRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));

        Operation operation = new Operation();
        operation.setSupplier(supplier);
        applyFields(operation, request);

        return OperationSummaryResponse.from(operationRepository.save(operation));
    }

    @Transactional
    public OperationSummaryResponse update(UUID id, OperationRequest request) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found: " + id));

        applyFields(operation, request);
        return OperationSummaryResponse.from(operationRepository.save(operation));
    }

    private void applyFields(Operation operation, OperationRequest request) {
        operation.setContainer(request.getContainer());
        operation.setDescription(request.getDescription());
        operation.setTotalAmount(request.getTotalAmount());
        operation.setOrigin(request.getOrigin());
        operation.setStartDate(request.getStartDate());
        operation.setEndDate(request.getEndDate());
        operation.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            operation.setStatus(request.getStatus());
        }
    }

    private Specification<Operation> buildSpec(String status, UUID supplierId, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (supplierId != null) predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
