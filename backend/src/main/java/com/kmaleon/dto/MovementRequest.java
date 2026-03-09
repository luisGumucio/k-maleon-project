package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public class MovementRequest {

    @NotNull
    private UUID operationId;

    @NotBlank
    private String type;

    private String paymentType;

    @NotNull
    @Positive
    private Long amount;

    private String currency;

    @NotNull
    private LocalDate date;

    private String description;

    private String metadata;

    private String attachmentUrl;

    private UUID createdBy;

    public UUID getOperationId() { return operationId; }
    public void setOperationId(UUID operationId) { this.operationId = operationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
