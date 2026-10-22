package com.torquegestao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ServiceOrderItemRequest(
        @NotNull(message = "Peça é obrigatória") UUID partId,
        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero") Integer quantity) {
}
