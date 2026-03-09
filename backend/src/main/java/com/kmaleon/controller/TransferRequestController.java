package com.kmaleon.controller;

import com.kmaleon.dto.CreateTransferRequestDto;
import com.kmaleon.dto.TransferRequestItemResponse;
import com.kmaleon.service.TransferRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public List<TransferRequestItemResponse> getAll(@RequestParam(required = false) String status) {
        return transferRequestService.findAll(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferRequestItemResponse create(@Valid @RequestBody CreateTransferRequestDto request) {
        return transferRequestService.create(request);
    }

    @PostMapping("/{id}/complete")
    public TransferRequestItemResponse complete(@PathVariable UUID id) {
        return transferRequestService.complete(id);
    }

    @PostMapping("/{id}/reject")
    public TransferRequestItemResponse reject(@PathVariable UUID id) {
        return transferRequestService.reject(id);
    }
}
