package com.mecanicaplus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Propositalmente SEM campo "role": o autocadastro público sempre cria RECEPTIONIST.
 * Papéis elevados só podem ser atribuídos por um ADMIN via POST /api/auth/users.
 */
public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres") String password) {
}
