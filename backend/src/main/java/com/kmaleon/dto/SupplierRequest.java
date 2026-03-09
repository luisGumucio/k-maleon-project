package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;

public class SupplierRequest {

    @NotBlank
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
