package com.mecanicaplus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mecanicaplus.domain.Client;
import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String cpf,
        String phone,
        String email,
        String address,
        Instant createdAt,
        @JsonProperty("isActive") boolean active,
        int vehicleCount) {

    public static ClientResponse from(Client c, int vehicleCount) {
        return new ClientResponse(
                c.getId(), c.getName(), c.getCpf(), c.getPhone(), c.getEmail(), c.getAddress(),
                c.getCreatedAt(), c.isActive(), vehicleCount);
    }
}
