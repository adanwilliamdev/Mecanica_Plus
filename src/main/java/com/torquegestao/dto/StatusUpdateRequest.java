package com.torquegestao.dto;

import com.torquegestao.domain.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status é obrigatório") ServiceOrderStatus status, String diagnosis) {
}
