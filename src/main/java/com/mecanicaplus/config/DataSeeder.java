package com.mecanicaplus.config;

import com.mecanicaplus.domain.Role;
import com.mecanicaplus.repository.UserRepository;
import com.mecanicaplus.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Cria o usuário administrador inicial, se ainda não existir ({@code app.seed.*}). */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppProperties props;
    private final UserRepository users;
    private final AuthService authService;

    public DataSeeder(AppProperties props, UserRepository users, AuthService authService) {
        this.props = props;
        this.users = users;
        this.authService = authService;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Seed seed = props.getSeed();
        if (!seed.isEnabled()) {
            return;
        }
        boolean defaultPassword = AppProperties.DEFAULT_ADMIN_PASSWORD.equals(seed.getAdminPassword());
        if (props.isProduction() && defaultPassword) {
            log.warn("Seed do administrador ignorado: defina SEED_ADMIN_PASSWORD em produção.");
            return;
        }
        if (users.existsByEmail(seed.getAdminEmail())) {
            log.info("Usuário admin já existe, pulando seed.");
            return;
        }
        authService.createUser("Administrador", seed.getAdminEmail(), seed.getAdminPassword(), Role.ADMIN);
        log.info("Usuário admin criado: {}", seed.getAdminEmail());
        if (defaultPassword) {
            log.warn("Senha padrão em uso — defina SEED_ADMIN_PASSWORD fora de desenvolvimento.");
        }
    }
}
