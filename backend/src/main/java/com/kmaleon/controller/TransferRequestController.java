package com.kmaleon.controller;

import com.kmaleon.dto.CreateTransferRequestDto;
import com.kmaleon.dto.TransferRequestItemResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.TransferRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/transfer-requests")
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    public TransferRequestController(TransferRequestService transferRequestService) {
        this.transferRequestService = transferRequestService;
    }

    @GetMapping
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<TransferRequestItemResponse> getAll(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String status) {
        return transferRequestService.findAll(caller, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.ENCARGADO_OR_ADMIN)
    public TransferRequestItemResponse create(@Valid @RequestBody CreateTransferRequestDto request) {
        return transferRequestService.create(request);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize(Roles.ALMACENERO_OR_ADMIN)
    public TransferRequestItemResponse complete(@PathVariable UUID id) {
        return transferRequestService.complete(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize(Roles.ALMACENERO_OR_ADMIN)
    public TransferRequestItemResponse reject(@PathVariable UUID id) {
        return transferRequestService.reject(id);
    }
}
