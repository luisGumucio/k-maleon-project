package com.kmaleon.dto;

import com.kmaleon.model.AccountMovement;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MovementResponse {

    private UUID id;
    private UUID operationId;
    private String type;
    private String paymentType;
    private Long amount;
    private String currency;
    private LocalDate date;
    private String description;
    private String metadata;
    private String attachmentUrl;
    private UUID createdBy;
    private OffsetDateTime createdAt;

    private MovementResponse() {}

    public static MovementResponse from(AccountMovement movement) {
        return new Builder()
                .id(movement.getId())
                .operationId(movement.getOperation().getId())
                .type(movement.getType())
                .paymentType(movement.getPaymentType())
                .amount(movement.getAmount())
                .currency(movement.getCurrency())
                .date(movement.getDate())
                .description(movement.getDescription())
                .metadata(movement.getMetadata())
                .attachmentUrl(movement.getAttachmentUrl())
                .createdBy(movement.getCreatedBy())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MovementResponse response = new MovementResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder operationId(UUID operationId) { response.operationId = operationId; return this; }
        public Builder type(String type) { response.type = type; return this; }
        public Builder paymentType(String paymentType) { response.paymentType = paymentType; return this; }
        public Builder amount(Long amount) { response.amount = amount; return this; }
        public Builder currency(String currency) { response.currency = currency; return this; }
        public Builder date(LocalDate date) { response.date = date; return this; }
        public Builder description(String description) { response.description = description; return this; }
        public Builder metadata(String metadata) { response.metadata = metadata; return this; }
        public Builder attachmentUrl(String attachmentUrl) { response.attachmentUrl = attachmentUrl; return this; }
        public Builder createdBy(UUID createdBy) { response.createdBy = createdBy; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { response.createdAt = createdAt; return this; }

        public MovementResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public UUID getOperationId() { return operationId; }
    public String getType() { return type; }
    public String getPaymentType() { return paymentType; }
    public Long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public String getMetadata() { return metadata; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public UUID getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
