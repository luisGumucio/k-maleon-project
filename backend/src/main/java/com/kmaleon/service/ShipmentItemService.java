package com.kmaleon.service;

import com.kmaleon.dto.ShipmentDetailResponse;
import com.kmaleon.dto.ShipmentItemRequest;
import com.kmaleon.dto.ShipmentItemResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Shipment;
import com.kmaleon.model.ShipmentItem;
import com.kmaleon.repository.ShipmentItemRepository;
import com.kmaleon.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShipmentItemService {

    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentRepository shipmentRepository;

    public ShipmentItemService(ShipmentItemRepository shipmentItemRepository,
                               ShipmentRepository shipmentRepository) {
        this.shipmentItemRepository = shipmentItemRepository;
        this.shipmentRepository = shipmentRepository;
    }

    public ShipmentDetailResponse findByShipmentId(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentId));

        List<ShipmentItemResponse> items = shipmentItemRepository
                .findByShipmentIdOrderByCreatedAtAsc(shipmentId)
                .stream()
                .map(ShipmentItemResponse::from)
                .toList();

        return ShipmentDetailResponse.from(shipment, items);
    }

    @Transactional
    public ShipmentItemResponse create(ShipmentItemRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + request.getShipmentId()));

        ShipmentItem item = new ShipmentItem();
        item.setShipment(shipment);
        applyFields(item, request);

        return ShipmentItemResponse.from(shipmentItemRepository.save(item));
    }

    @Transactional
    public ShipmentItemResponse update(UUID id, ShipmentItemRequest request) {
        ShipmentItem item = shipmentItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShipmentItem not found: " + id));

        applyFields(item, request);
        return ShipmentItemResponse.from(shipmentItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        if (!shipmentItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("ShipmentItem not found: " + id);
        }
        shipmentItemRepository.deleteById(id);
    }

    private void applyFields(ShipmentItem item, ShipmentItemRequest request) {
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setAmount(request.getAmount());
    }
}
