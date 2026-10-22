package com.mecanicaplus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mecanicaplus.domain.Vehicle;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String plate,
        String brand,
        String model,
        int year,
        Integer mileage,
        String fuelType,
        UUID clientId,
        String clientName,
        Instant createdAt,
        @JsonProperty("isActive") boolean active) {

    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(
                v.getId(), v.getPlate(), v.getBrand(), v.getModel(), v.getYear(), v.getMileage(),
                v.getFuelType(), v.getClient().getId(), v.getClient().getName(), v.getCreatedAt(),
                v.isActive());
    }
}
