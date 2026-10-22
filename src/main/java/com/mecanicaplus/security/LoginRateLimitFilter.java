package com.mecanicaplus.security;

import com.mecanicaplus.config.AppProperties;
import com.mecanicaplus.service.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.web.filter.OncePerRequestFilter;

/** Limita tentativas do formulário de login (por IP + e-mail), via Redis (fail-open). */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final CacheService cache;
    private final AppProperties props;

    public LoginRateLimitFilter(CacheService cache, AppProperties props) {
        this.cache = cache;
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String key = "rl:login:" + request.getRemoteAddr() + ":"
                + (email == null ? "" : email.toLowerCase(Locale.ROOT));
        if (cache.hitRateLimit(
                key, props.getLoginRateLimitAttempts(), props.getLoginRateLimitWindowSeconds())) {
            response.sendRedirect(request.getContextPath() + "/login?rate");
            return;
        }
        chain.doFilter(request, response);
    }
}
