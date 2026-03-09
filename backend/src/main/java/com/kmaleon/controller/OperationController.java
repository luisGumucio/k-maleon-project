package com.kmaleon.controller;

import com.kmaleon.dto.OperationRequest;
import com.kmaleon.dto.OperationSummaryResponse;
import com.kmaleon.service.OperationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping
    public List<OperationSummaryResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return operationService.findAll(status, supplierId, from, to);
    }

    @GetMapping("/{id}")
    public OperationSummaryResponse findById(@PathVariable UUID id) {
        return operationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationSummaryResponse create(@Valid @RequestBody OperationRequest request) {
        return operationService.create(request);
    }

    @PutMapping("/{id}")
    public OperationSummaryResponse update(@PathVariable UUID id, @Valid @RequestBody OperationRequest request) {
        return operationService.update(id, request);
    }
}
