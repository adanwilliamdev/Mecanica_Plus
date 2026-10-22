package com.mecanicaplus.security;

import com.mecanicaplus.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Recarrega o usuário da sessão web a cada requisição para que mudança de papel ou desativação
 * de conta tenham efeito imediato (mesma garantia dada à API).
 */
public class SessionUserRefreshFilter extends OncePerRequestFilter {

    private final UserRepository users;

    public SessionUserRefreshFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.getPrincipal() instanceof AppUserDetails principal) {
            var fresh = users.findById(principal.getId()).filter(u -> u.isActive());
            if (fresh.isEmpty()) {
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            } else {
                var details = new AppUserDetails(fresh.get());
                var refreshed = UsernamePasswordAuthenticationToken.authenticated(
                        details, null, details.getAuthorities());
                refreshed.setDetails(current.getDetails());
                SecurityContextHolder.getContext().setAuthentication(refreshed);
            }
        }
        chain.doFilter(request, response);
    }
}
