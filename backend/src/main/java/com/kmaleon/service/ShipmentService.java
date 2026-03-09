package com.kmaleon.service;

import com.kmaleon.dto.ShipmentRequest;
import com.kmaleon.dto.ShipmentResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Shipment;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.ShipmentRepository;
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
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SupplierRepository supplierRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, SupplierRepository supplierRepository) {
        this.shipmentRepository = shipmentRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<ShipmentResponse> findAll(UUID callerId, String callerRole,
                                          UUID supplierId, String containerNumber,
                                          LocalDate from, LocalDate to) {
        UUID ownerFilter = "super_admin".equals(callerRole) ? null : callerId;
        return shipmentRepository.findAll(buildSpec(ownerFilter, supplierId, containerNumber, from, to)).stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    public ShipmentResponse findById(UUID id, UUID callerId, String callerRole) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
        if (!"super_admin".equals(callerRole) && !callerId.equals(shipment.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este rastreo");
        }
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse create(UUID callerId, ShipmentRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));

        Shipment shipment = new Shipment();
        shipment.setOwnerId(callerId);
        shipment.setSupplier(supplier);
        applyFields(shipment, request);

        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse update(UUID id, UUID callerId, String callerRole, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
        if (!"super_admin".equals(callerRole) && !callerId.equals(shipment.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este rastreo");
        }

        if (!shipment.getSupplier().getId().equals(request.getSupplierId())) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));
            shipment.setSupplier(supplier);
        }

        applyFields(shipment, request);
        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    @Transactional
    public void delete(UUID id, UUID callerId, String callerRole) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
        if (!"super_admin".equals(callerRole) && !callerId.equals(shipment.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este rastreo");
        }
        shipmentRepository.deleteById(id);
    }

    private void applyFields(Shipment shipment, ShipmentRequest request) {
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setContainerNumber(request.getContainerNumber());
        shipment.setQuantity(request.getQuantity());
        shipment.setProductDetails(request.getProductDetails());
        shipment.setArrivalDate(request.getArrivalDate());
        if (request.getDocumentUrl() != null) {
            shipment.setDocumentUrl(request.getDocumentUrl());
        }
    }

    private Specification<Shipment> buildSpec(UUID ownerId, UUID supplierId,
                                               String containerNumber, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerId != null) predicates.add(cb.equal(root.get("ownerId"), ownerId));
            if (supplierId != null) predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            if (containerNumber != null && !containerNumber.isBlank())
                predicates.add(cb.like(cb.lower(root.get("containerNumber")), "%" + containerNumber.toLowerCase() + "%"));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("departureDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("departureDate"), to));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
