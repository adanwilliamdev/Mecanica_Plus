package com.torquegestao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LaborAmountRequest(
        @NotNull(message = "Valor da mão de obra é obrigatório")
        @DecimalMin(value = "0", message = "Mão de obra não pode ser negativa")
        @Digits(integer = 10, fraction = 2, message = "Valor inválido") BigDecimal laborAmount) {
}
