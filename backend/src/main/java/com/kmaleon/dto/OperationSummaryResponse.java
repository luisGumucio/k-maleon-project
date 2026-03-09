package com.kmaleon.dto;

import com.kmaleon.model.Operation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class OperationSummaryResponse {

    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private String container;
    private String description;
    private Long totalAmount;
    private Long paidAmount;
    private Long pendingAmount;
    private String origin;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private OperationSummaryResponse() {}

    public static OperationSummaryResponse from(Operation operation) {
        return new Builder()
                .id(operation.getId())
                .supplierId(operation.getSupplier().getId())
                .supplierName(operation.getSupplier().getName())
                .container(operation.getContainer())
                .description(operation.getDescription())
                .totalAmount(operation.getTotalAmount())
                .paidAmount(operation.getPaidAmount())
                .pendingAmount(operation.getTotalAmount() - operation.getPaidAmount())
                .origin(operation.getOrigin())
                .startDate(operation.getStartDate())
                .endDate(operation.getEndDate())
                .status(operation.getStatus())
                .notes(operation.getNotes())
                .createdAt(operation.getCreatedAt())
                .updatedAt(operation.getUpdatedAt())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OperationSummaryResponse response = new OperationSummaryResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder supplierId(UUID supplierId) { response.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { response.supplierName = supplierName; return this; }
        public Builder container(String container) { response.container = container; return this; }
        public Builder description(String description) { response.description = description; return this; }
        public Builder totalAmount(Long totalAmount) { response.totalAmount = totalAmount; return this; }
        public Builder paidAmount(Long paidAmount) { response.paidAmount = paidAmount; return this; }
        public Builder pendingAmount(Long pendingAmount) { response.pendingAmount = pendingAmount; return this; }
        public Builder origin(String origin) { response.origin = origin; return this; }
        public Builder startDate(LocalDate startDate) { response.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { response.endDate = endDate; return this; }
        public Builder status(String status) { response.status = status; return this; }
        public Builder notes(String notes) { response.notes = notes; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { response.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { response.updatedAt = updatedAt; return this; }

        public OperationSummaryResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public UUID getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getContainer() { return container; }
    public String getDescription() { return description; }
    public Long getTotalAmount() { return totalAmount; }
    public Long getPaidAmount() { return paidAmount; }
    public Long getPendingAmount() { return pendingAmount; }
    public String getOrigin() { return origin; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
