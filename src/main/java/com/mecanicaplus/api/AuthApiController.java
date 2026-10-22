package com.mecanicaplus.api;

import com.mecanicaplus.dto.CreateUserRequest;
import com.mecanicaplus.dto.LoginRequest;
import com.mecanicaplus.dto.LoginResponse;
import com.mecanicaplus.dto.RefreshTokenRequest;
import com.mecanicaplus.dto.RegisterRequest;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService auth;

    public AuthApiController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest data, HttpServletRequest request) {
        return auth.login(data, request.getRemoteAddr());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest data) {
        return auth.refresh(data);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest data) {
        auth.logout(data);
    }

    /** Autocadastro público. Sempre cria a conta como RECEPTIONIST. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest data) {
        auth.register(data);
    }

    /** Criação de usuário com papel arbitrário. Restrito a ADMIN. */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.ADMIN_ONLY)
    public void createUser(@Valid @RequestBody CreateUserRequest data) {
        auth.createUserByAdmin(data);
    }
}
