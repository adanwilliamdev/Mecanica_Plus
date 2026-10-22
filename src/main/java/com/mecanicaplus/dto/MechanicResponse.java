package com.mecanicaplus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mecanicaplus.domain.Mechanic;
import java.time.Instant;
import java.util.UUID;

public record MechanicResponse(
        UUID id,
        String name,
        String specialty,
        String phone,
        @JsonProperty("isAvailable") boolean available,
        Instant createdAt,
        @JsonProperty("isActive") boolean active) {

    public static MechanicResponse from(Mechanic m) {
        return new MechanicResponse(
                m.getId(), m.getName(), m.getSpecialty(), m.getPhone(), m.isAvailable(),
                m.getCreatedAt(), m.isActive());
    }
}
