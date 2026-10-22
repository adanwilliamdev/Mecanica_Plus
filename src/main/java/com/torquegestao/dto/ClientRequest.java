package com.torquegestao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        String cpf,
        String phone,
        @Email(message = "Email inválido") String email,
        String address) {
}
