package com.mecanicaplus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record VehicleRequest(
        @NotBlank(message = "Placa é obrigatória")
        @Pattern(regexp = VehicleRequest.PLATE_PATTERN, message = "Placa inválida (use ABC1D23 ou ABC1234)")
        String plate,
        @NotBlank(message = "Marca é obrigatória") String brand,
        @NotBlank(message = "Modelo é obrigatório") String model,
        @NotNull(message = "Ano é obrigatório") @Min(value = 1, message = "Ano inválido") Integer year,
        @Min(value = 0, message = "Quilometragem não pode ser negativa") Integer mileage,
        String fuelType,
        @NotNull(message = "Cliente é obrigatório") UUID clientId) {

    public static final String PLATE_PATTERN = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$";

    /** Normaliza a placa (maiúsculas, sem hífen e sem espaços nas pontas) antes da validação. */
    public VehicleRequest {
        plate = normalizePlate(plate);
    }

    public static String normalizePlate(String raw) {
        return raw == null ? null : raw.trim().toUpperCase().replace("-", "");
    }
}
