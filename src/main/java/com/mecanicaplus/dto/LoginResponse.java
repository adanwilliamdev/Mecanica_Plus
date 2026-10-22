package com.mecanicaplus.dto;

import com.mecanicaplus.domain.Role;

public record LoginResponse(
        String token, String refreshToken, String userId, String name, String email, Role role) {
}
