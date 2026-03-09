package com.kmaleon.controller;

import com.kmaleon.dto.MovementRequest;
import com.kmaleon.dto.MovementResponse;
import com.kmaleon.security.Roles;
import com.kmaleon.service.MovementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class MovementController {

    private final MovementService movementService;

    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping("/api/operations/{operationId}/movements")
    public List<MovementResponse> findByOperation(@PathVariable UUID operationId) {
        return movementService.findByOperationId(operationId);
    }

    @PostMapping("/api/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse register(@Valid @RequestBody MovementRequest request) {
        return movementService.registerMovement(request);
    }
}
