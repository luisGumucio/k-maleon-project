package com.kmaleon.controller;

import com.kmaleon.dto.OperationRequest;
import com.kmaleon.dto.OperationSummaryResponse;
import com.kmaleon.security.AuthenticatedUser;
import com.kmaleon.security.Roles;
import com.kmaleon.service.OperationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operations")
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping
    public List<OperationSummaryResponse> findAll(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return operationService.findAll(caller.getId(), caller.getRole(), status, supplierId, from, to);
    }

    @GetMapping("/{id}")
    public OperationSummaryResponse findById(@PathVariable UUID id,
                                             @AuthenticationPrincipal AuthenticatedUser caller) {
        return operationService.findById(id, caller.getId(), caller.getRole());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationSummaryResponse create(@Valid @RequestBody OperationRequest request,
                                           @AuthenticationPrincipal AuthenticatedUser caller) {
        return operationService.create(caller.getId(), request);
    }

    @PutMapping("/{id}")
    public OperationSummaryResponse update(@PathVariable UUID id,
                                           @Valid @RequestBody OperationRequest request,
                                           @AuthenticationPrincipal AuthenticatedUser caller) {
        return operationService.update(id, caller.getId(), caller.getRole(), request);
    }
}
