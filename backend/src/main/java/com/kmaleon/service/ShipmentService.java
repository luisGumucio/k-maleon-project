package com.kmaleon.service;

import com.kmaleon.dto.ShipmentRequest;
import com.kmaleon.dto.ShipmentResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Shipment;
import com.kmaleon.model.Supplier;
import com.kmaleon.repository.ShipmentRepository;
import com.kmaleon.repository.SupplierRepository;
import java.time.LocalDate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ShipmentResponse> findAll(UUID supplierId, String containerNumber, LocalDate from, LocalDate to) {
        return shipmentRepository.findAll(buildSpec(supplierId, containerNumber, from, to)).stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    public ShipmentResponse findById(UUID id) {
        return shipmentRepository.findById(id)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
    }

    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));

        Shipment shipment = new Shipment();
        shipment.setSupplier(supplier);
        applyFields(shipment, request);

        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse update(UUID id, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));

        if (!shipment.getSupplier().getId().equals(request.getSupplierId())) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));
            shipment.setSupplier(supplier);
        }

        applyFields(shipment, request);
        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    @Transactional
    public void delete(UUID id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment not found: " + id);
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

    private Specification<Shipment> buildSpec(UUID supplierId, String containerNumber, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
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
