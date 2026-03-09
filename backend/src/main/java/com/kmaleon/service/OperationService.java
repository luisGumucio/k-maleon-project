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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public List<OperationSummaryResponse> findAll(UUID callerId, String callerRole,
                                                   String status, UUID supplierId,
                                                   LocalDate from, LocalDate to) {
        UUID ownerFilter = "super_admin".equals(callerRole) ? null : callerId;
        return operationRepository.findAll(buildSpec(ownerFilter, status, supplierId, from, to)).stream()
                .map(OperationSummaryResponse::from)
                .toList();
    }

    public OperationSummaryResponse findById(UUID id, UUID callerId, String callerRole) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found: " + id));

        if (!"super_admin".equals(callerRole) && !callerId.equals(operation.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta operación");
        }
        return OperationSummaryResponse.from(operation);
    }

    @Transactional
    public OperationSummaryResponse create(UUID callerId, OperationRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));

        Operation operation = new Operation();
        operation.setOwnerId(callerId);
        operation.setSupplier(supplier);
        applyFields(operation, request);

        return OperationSummaryResponse.from(operationRepository.save(operation));
    }

    @Transactional
    public OperationSummaryResponse update(UUID id, UUID callerId, String callerRole, OperationRequest request) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found: " + id));

        if (!"super_admin".equals(callerRole) && !callerId.equals(operation.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta operación");
        }

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

    private Specification<Operation> buildSpec(UUID ownerId, String status, UUID supplierId,
                                                LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerId != null) predicates.add(cb.equal(root.get("ownerId"), ownerId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (supplierId != null) predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), to));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
