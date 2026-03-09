package com.kmaleon.dto;

import com.kmaleon.model.Supplier;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SupplierResponse {

    private UUID id;
    private String name;
    private OffsetDateTime createdAt;

    private SupplierResponse() {}

    public static SupplierResponse from(Supplier supplier) {
        return new Builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .createdAt(supplier.getCreatedAt())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SupplierResponse response = new SupplierResponse();

        public Builder id(UUID id) { response.id = id; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { response.createdAt = createdAt; return this; }

        public SupplierResponse build() { return response; }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
