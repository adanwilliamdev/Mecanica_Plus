package com.torquegestao.security;

import com.torquegestao.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica requisições da API via {@code Authorization: Bearer}. O usuário é buscado no banco a
 * cada requisição (e não confiado ao payload do token) para respeitar imediatamente mudanças de
 * papel e desativação de contas.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            authenticate(request, header.substring(7).trim());
        }
        chain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        UUID userId;
        try {
            userId = jwtService.parseUserId(token);
        } catch (JwtException | IllegalArgumentException ex) {
            request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTRIBUTE, "Token inválido ou expirado");
            return;
        }
        var user = users.findById(userId).filter(u -> u.isActive());
        if (user.isEmpty()) {
            request.setAttribute(
                    ApiErrorWriter.AUTH_ERROR_ATTRIBUTE, "Usuário não encontrado ou inativo");
            return;
        }
        var principal = new AppUserDetails(user.get());
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
