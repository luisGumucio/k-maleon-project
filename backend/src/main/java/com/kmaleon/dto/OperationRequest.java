package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public class OperationRequest {

    @NotNull
    private UUID supplierId;

    @NotBlank
    private String container;

    private String description;

    @NotNull
    @Positive
    private Long totalAmount;

    private String origin;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String notes;

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public String getContainer() { return container; }
    public void setContainer(String container) { this.container = container; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
