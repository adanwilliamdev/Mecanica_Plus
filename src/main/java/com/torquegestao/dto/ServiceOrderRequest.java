package com.torquegestao.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ServiceOrderRequest(
        @NotNull(message = "Cliente é obrigatório") UUID clientId,
        @NotNull(message = "Veículo é obrigatório") UUID vehicleId,
        UUID mechanicId,
        String reportedProblem) {
}
