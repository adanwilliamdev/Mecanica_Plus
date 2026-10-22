package com.mecanicaplus.dto;

import com.mecanicaplus.domain.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status é obrigatório") ServiceOrderStatus status, String diagnosis) {
}
