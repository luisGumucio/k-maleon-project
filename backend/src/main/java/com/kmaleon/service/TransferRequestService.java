package com.kmaleon.service;

import com.kmaleon.dto.CreateTransferRequestDto;
import com.kmaleon.dto.TransferRequestItemResponse;
import com.kmaleon.model.Item;
import com.kmaleon.model.Location;
import com.kmaleon.model.TransferRequest;
import com.kmaleon.model.Unit;
import com.kmaleon.model.UnitConversion;
import com.kmaleon.repository.ItemRepository;
import com.kmaleon.repository.LocationRepository;
import com.kmaleon.repository.TransferRequestRepository;
import com.kmaleon.repository.UnitConversionRepository;
import com.kmaleon.repository.UnitRepository;
import com.kmaleon.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final UnitConversionRepository unitConversionRepository;
    private final ItemRepository itemRepository;
    private final UnitRepository unitRepository;
    private final LocationRepository locationRepository;
    private final InventoryMovementService inventoryMovementService;

    public TransferRequestService(TransferRequestRepository transferRequestRepository,
                                  UnitConversionRepository unitConversionRepository,
                                  ItemRepository itemRepository,
                                  UnitRepository unitRepository,
                                  LocationRepository locationRepository,
                                  InventoryMovementService inventoryMovementService) {
        this.transferRequestRepository = transferRequestRepository;
        this.unitConversionRepository = unitConversionRepository;
        this.itemRepository = itemRepository;
        this.unitRepository = unitRepository;
        this.locationRepository = locationRepository;
        this.inventoryMovementService = inventoryMovementService;
    }

    public List<TransferRequestItemResponse> findAll(AuthenticatedUser caller, String status) {
        boolean isEncargado = "encargado_sucursal".equals(caller.getRole());
        UUID locationId = isEncargado ? caller.getLocationId() : null;

        List<TransferRequest> requests;
        if (isEncargado && locationId != null) {
            requests = (status != null && !status.isEmpty())
                    ? transferRequestRepository.findByLocationIdAndStatusOrderByCreatedAtDesc(locationId, status)
                    : transferRequestRepository.findByLocationIdOrderByCreatedAtDesc(locationId);
        } else {
            requests = (status != null && !status.isEmpty())
                    ? transferRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                    : transferRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferRequestItemResponse create(CreateTransferRequestDto requestDto) {
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        Unit unit = unitRepository.findById(requestDto.getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
        Location location = locationRepository.findById(requestDto.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada"));

        BigDecimal quantityBase = requestDto.getQuantity();

        if (!item.getBaseUnit().getId().equals(unit.getId())) {
            UnitConversion conversion = unitConversionRepository
                    .findById(unit.getId()) // Buscamos por la unidad (en app real quizas debamos buscar por itemId y fromUnitId)
                    .orElse(null);

            if (conversion == null) {
                // Buscamos manual
                conversion = unitConversionRepository.findByItemId(item.getId()).stream()
                    .filter(c -> c.getFromUnit().getId().equals(unit.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No hay conversión configurada para esta unidad."));
            }

            quantityBase = requestDto.getQuantity().multiply(conversion.getFactor());
        }

        TransferRequest req = new TransferRequest();
        req.setItem(item);
        req.setUnit(unit);
        req.setQuantity(requestDto.getQuantity());
        req.setQuantityBase(quantityBase);
        req.setLocation(location);
        req.setNotes(requestDto.getNotes());
        req.setStatus("pending");

        req = transferRequestRepository.save(req);
        return mapToResponse(req);
    }

    @Transactional
    public TransferRequestItemResponse complete(UUID id) {
        TransferRequest req = transferRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!"pending".equals(req.getStatus())) {
            throw new IllegalStateException("Solo se pueden completar solicitudes en estado 'pending'");
        }

        Location warehouse = locationRepository.findByTypeAndActiveTrue("warehouse").stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay bodega activa configurada."));

        com.kmaleon.dto.TransferRequestDto invReq = new com.kmaleon.dto.TransferRequestDto();
        invReq.setItemId(req.getItem().getId());
        invReq.setQuantity(req.getQuantity());
        invReq.setUnitId(req.getUnit().getId());
        invReq.setLocationToId(req.getLocation().getId());
        invReq.setNotes("S/Solicitud: " + (req.getNotes() != null ? req.getNotes() : ""));
        
        // El metodo nativo del inventoryMovementService usa locationId como SUBSCUSAL DESTINO en transferencias
        inventoryMovementService.transfer(invReq);

        req.setStatus("completed");
        req = transferRequestRepository.save(req);
        
        return mapToResponse(req);
    }

    @Transactional
    public TransferRequestItemResponse reject(UUID id) {
        TransferRequest req = transferRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!"pending".equals(req.getStatus())) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes en estado 'pending'");
        }

        req.setStatus("rejected");
        req = transferRequestRepository.save(req);
        
        return mapToResponse(req);
    }

    private TransferRequestItemResponse mapToResponse(TransferRequest req) {
        TransferRequestItemResponse res = new TransferRequestItemResponse();
        res.setId(req.getId());
        res.setItemId(req.getItem().getId());
        res.setItemName(req.getItem().getName());
        res.setUnitId(req.getUnit().getId());
        res.setUnitName(req.getUnit().getName());
        res.setUnitSymbol(req.getUnit().getSymbol());
        res.setQuantity(req.getQuantity());
        res.setQuantityBase(req.getQuantityBase());
        res.setLocationId(req.getLocation().getId());
        res.setLocationName(req.getLocation().getName());
        res.setStatus(req.getStatus());
        res.setNotes(req.getNotes());
        res.setCreatedAt(req.getCreatedAt());
        res.setUpdatedAt(req.getUpdatedAt());
        return res;
    }
}
