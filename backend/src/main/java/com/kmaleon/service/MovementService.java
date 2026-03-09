package com.kmaleon.service;

import com.kmaleon.dto.MovementRequest;
import com.kmaleon.dto.MovementResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Account;
import com.kmaleon.model.AccountMovement;
import com.kmaleon.model.Operation;
import com.kmaleon.repository.MovementRepository;
import com.kmaleon.repository.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovementService {

    private final MovementRepository movementRepository;
    private final OperationRepository operationRepository;
    private final AccountService accountService;
    private final AuditService auditService;

    public MovementService(MovementRepository movementRepository,
                           OperationRepository operationRepository,
                           AccountService accountService,
                           AuditService auditService) {
        this.movementRepository = movementRepository;
        this.operationRepository = operationRepository;
        this.accountService = accountService;
        this.auditService = auditService;
    }

    @Transactional
    public MovementResponse registerMovement(MovementRequest request) {
        Operation operation = operationRepository.findById(request.getOperationId())
                .orElseThrow(() -> new ResourceNotFoundException("Operation not found: " + request.getOperationId()));

        Account account = accountService.getAccount();

        AccountMovement movement = new AccountMovement();
        movement.setOperation(operation);
        movement.setType(request.getType());
        movement.setPaymentType(request.getPaymentType());
        movement.setAmount(request.getAmount());
        movement.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        movement.setDate(request.getDate());
        movement.setDescription(request.getDescription());
        movement.setMetadata(request.getMetadata());
        movement.setAttachmentUrl(request.getAttachmentUrl());
        movement.setCreatedBy(request.getCreatedBy());

        AccountMovement saved = movementRepository.save(movement);

        if ("salida".equals(request.getType())) {
            operation.setPaidAmount(operation.getPaidAmount() + request.getAmount());
            operationRepository.save(operation);
            accountService.debit(account, request.getAmount());
        } else {
            accountService.credit(account, request.getAmount());
        }

        auditService.log("movement_created", "account_movements", saved.getId(), request);

        return MovementResponse.from(saved);
    }

    public List<MovementResponse> findByOperationId(UUID operationId) {
        return movementRepository.findByOperationIdOrderByDateDesc(operationId).stream()
                .map(MovementResponse::from)
                .toList();
    }
}
