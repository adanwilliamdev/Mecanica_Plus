package com.mecanicaplus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mecanicaplus.domain.Part;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PartResponse(
        UUID id,
        String name,
        String code,
        String manufacturer,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        int stockQuantity,
        int minimumStock,
        @JsonProperty("isLowStock") boolean lowStock,
        Instant createdAt,
        @JsonProperty("isActive") boolean active) {

    public static PartResponse from(Part p) {
        return new PartResponse(
                p.getId(), p.getName(), p.getCode(), p.getManufacturer(), p.getPurchasePrice(),
                p.getSalePrice(), p.getStockQuantity(), p.getMinimumStock(), p.isLowStock(),
                p.getCreatedAt(), p.isActive());
    }
}
