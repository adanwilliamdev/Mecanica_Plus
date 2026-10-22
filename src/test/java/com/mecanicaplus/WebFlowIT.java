package com.mecanicaplus;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mecanicaplus.domain.Role;
import com.mecanicaplus.domain.User;
import com.mecanicaplus.repository.UserRepository;
import com.mecanicaplus.security.AppUserDetails;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Renderiza as páginas Thymeleaf de ponta a ponta para pegar erros de template. */
class WebFlowIT extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    private AppUserDetails principal(Role role) {
        User u = new User();
        u.setName("Teste " + role);
        u.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@teste.com");
        u.setPasswordHash(encoder.encode("x12345"));
        u.setRole(role);
        u.setActive(true);
        return new AppUserDetails(users.saveAndFlush(u));
    }

    @Test
    void todasAsListagensRenderizamParaOAdmin() throws Exception {
        var admin = user(principal(Role.ADMIN));
        for (String path : new String[] {"/", "/clients", "/vehicles", "/mechanics", "/service-orders",
                "/budgets", "/inventory", "/clients/new", "/vehicles/new", "/mechanics/new",
                "/service-orders/new", "/budgets/new", "/inventory/new"}) {
            mvc.perform(get(path).with(admin))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/html"));
        }
    }

    @Test
    void recepcionistaEhRedirecionadoDaHomeParaClientes() throws Exception {
        mvc.perform(get("/").with(user(principal(Role.RECEPTIONIST))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));
    }

    @Test
    void mecanicoNaoAcessaEstoque() throws Exception {
        mvc.perform(get("/inventory").with(user(principal(Role.MECHANIC))))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarClientePeloFormularioRedirecionaParaALista() throws Exception {
        var admin = user(principal(Role.ADMIN));
        mvc.perform(post("/clients").with(admin).with(csrf())
                        .param("name", "Cliente Web").param("email", "web@teste.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));
    }

    @Test
    void formularioInvalidoVoltaComErrosSemRedirecionar() throws Exception {
        var admin = user(principal(Role.ADMIN));
        mvc.perform(post("/clients").with(admin).with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nome é obrigatório")));
    }

    @Test
    void postSemCsrfEhRecusado() throws Exception {
        mvc.perform(post("/clients").with(user(principal(Role.ADMIN))).param("name", "X"))
                .andExpect(status().isForbidden());
    }
}
