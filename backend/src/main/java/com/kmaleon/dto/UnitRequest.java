package com.kmaleon.dto;

import jakarta.validation.constraints.NotBlank;

public class UnitRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "symbol is required")
    private String symbol;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
