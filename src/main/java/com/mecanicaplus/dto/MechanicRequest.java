package com.mecanicaplus.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MechanicRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        String specialty,
        String phone,
        /** Opcional: vincula o perfil a uma conta de login (User com perfil MECHANIC). */
        UUID userId) {
}
