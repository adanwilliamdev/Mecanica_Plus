package com.mecanicaplus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PartRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Código é obrigatório") String code,
        String manufacturer,
        @NotNull(message = "Preço de compra é obrigatório")
        @DecimalMin(value = "0", inclusive = false, message = "Preço de compra deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Preço de compra inválido") BigDecimal purchasePrice,
        @NotNull(message = "Preço de venda é obrigatório")
        @DecimalMin(value = "0", inclusive = false, message = "Preço de venda deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Preço de venda inválido") BigDecimal salePrice,
        @NotNull(message = "Estoque é obrigatório")
        @Min(value = 0, message = "Estoque não pode ser negativo") Integer stockQuantity,
        @Positive(message = "Estoque mínimo deve ser maior que zero") Integer minimumStock) {

    public static final int DEFAULT_MINIMUM_STOCK = 5;

    public int minimumStockOrDefault() {
        return minimumStock == null ? DEFAULT_MINIMUM_STOCK : minimumStock;
    }
}
