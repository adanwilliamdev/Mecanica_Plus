package com.torquegestao.dto;

import com.torquegestao.domain.Role;

public record LoginResponse(
        String token, String refreshToken, String userId, String name, String email, Role role) {
}
