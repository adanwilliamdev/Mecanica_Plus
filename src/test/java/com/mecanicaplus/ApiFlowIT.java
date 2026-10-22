package com.mecanicaplus;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mecanicaplus.domain.Role;
import com.mecanicaplus.domain.User;
import com.mecanicaplus.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class ApiFlowIT extends AbstractIntegrationTest {

    private static final String PASSWORD = "senha123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    // ------------------------------------------------------------------ helpers

    private String email(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@teste.com";
    }

    private User createUser(Role role) {
        User user = new User();
        user.setName("Usuário " + role);
        user.setEmail(email(role.name().toLowerCase()));
        user.setPasswordHash(encoder.encode(PASSWORD));
        user.setRole(role);
        user.setActive(true);
        return users.saveAndFlush(user);
    }

    private String tokenFor(User user) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private String adminToken() throws Exception {
        return tokenFor(createUser(Role.ADMIN));
    }

    private ResultActions send(String method, String url, String token, Object body) throws Exception {
        var builder = switch (method) {
            case "POST" -> post(url);
            case "PATCH" -> patch(url);
            case "DELETE" -> delete(url);
            default -> get(url);
        };
        builder.header("Authorization", "Bearer " + token);
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        }
        return mvc.perform(builder);
    }

    private JsonNode created(String url, String token, Object body) throws Exception {
        String content = send("POST", url, token, body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(content);
    }

    private String uniquePlate() {
        var rnd = java.util.concurrent.ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append((char) ('A' + rnd.nextInt(26)));
        }
        sb.append(rnd.nextInt(10)).append((char) ('A' + rnd.nextInt(26)))
                .append(rnd.nextInt(10)).append(rnd.nextInt(10));
        return sb.toString();
    }

    /** Compara valores monetários ignorando a escala (0 == 0.00 == 0.0). */
    private static org.hamcrest.Matcher<Object> amount(String expected) {
        return new org.hamcrest.TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Object actual) {
                return new java.math.BigDecimal(actual.toString()).compareTo(new java.math.BigDecimal(expected)) == 0;
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("valor igual a ").appendText(expected);
            }
        };
    }

    // ------------------------------------------------------------------ autenticação

    @Test
    void loginComCredenciaisValidasDevolveTokens() throws Exception {
        User user = createUser(Role.ADMIN);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        User user = createUser(Role.ADMIN);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", user.getEmail(), "password", "errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Email ou senha inválidos")));
    }

    @Test
    void refreshTokenRodaERecusaReuso() throws Exception {
        User user = createUser(Role.ADMIN);
        String loginBody = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andReturn().getResponse().getContentAsString();
        String refresh = json.readTree(loginBody).get("refreshToken").asText();
        String payload = json.writeValueAsString(Map.of("refreshToken", refresh));

        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autocadastroPublicoSempreCriaRecepcionista() throws Exception {
        String mail = email("novo");
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("name", "Novo", "email", mail, "password", "senha123", "role", "ADMIN"))))
                .andExpect(status().isCreated());
        org.junit.jupiter.api.Assertions.assertEquals(Role.RECEPTIONIST, users.findByEmail(mail).orElseThrow().getRole());
    }

    @Test
    void semTokenRetorna401EmJson() throws Exception {
        mvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/api/clients").header("Authorization", "Bearer lixo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token inválido ou expirado")));
    }

    // ------------------------------------------------------------------ RBAC

    @Test
    void recepcionistaNaoCriaPecaMasCriaCliente() throws Exception {
        String token = tokenFor(createUser(Role.RECEPTIONIST));
        Map<String, Object> part = Map.of("name", "Filtro", "code", "F-" + UUID.randomUUID(),
                "purchasePrice", 10, "salePrice", 20, "stockQuantity", 5);
        send("POST", "/api/inventory/parts", token, part)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Você não tem permissão para executar esta ação")));
        send("POST", "/api/clients", token, Map.of("name", "Maria")).andExpect(status().isCreated());
    }

    @Test
    void gerenteNaoCriaClienteMasLista() throws Exception {
        String token = tokenFor(createUser(Role.MANAGER));
        send("POST", "/api/clients", token, Map.of("name", "Maria")).andExpect(status().isForbidden());
        send("GET", "/api/clients", token, null).andExpect(status().isOk());
    }

    @Test
    void mecanicoSemPerfilNaoEnxergaOrdens() throws Exception {
        String admin = adminToken();
        JsonNode order = novaOrdem(admin, null);
        String mecanico = tokenFor(createUser(Role.MECHANIC));

        send("GET", "/api/service-orders", mecanico, null).andExpect(status().isForbidden());
        send("GET", "/api/service-orders/" + order.get("id").asText(), mecanico, null)
                .andExpect(status().isForbidden());
        send("GET", "/api/service-orders/mine", mecanico, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    // ------------------------------------------------------------------ validação

    @Test
    void validacaoRetorna422ComListaDeErros() throws Exception {
        String admin = adminToken();
        send("POST", "/api/clients", admin, Map.of("name", ""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors", hasItem("name: Nome é obrigatório")));
    }

    @Test
    void placaInvalidaRetorna422EDuplicadaRetorna400() throws Exception {
        String admin = adminToken();
        JsonNode client = created("/api/clients", admin, Map.of("name", "Cliente Placa"));
        Map<String, Object> bad = Map.of("plate", "XXXX", "brand", "VW", "model", "Gol", "year", 2010,
                "clientId", client.get("id").asText());
        send("POST", "/api/vehicles", admin, bad).andExpect(status().isUnprocessableEntity());

        String plate = uniquePlate();
        Map<String, Object> ok = Map.of("plate", plate.toLowerCase(), "brand", "VW", "model", "Gol",
                "year", 2010, "clientId", client.get("id").asText());
        send("POST", "/api/vehicles", admin, ok)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plate", is(plate)));
        send("POST", "/api/vehicles", admin, ok)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Já existe um veículo com esta placa: " + plate)));
    }

    @Test
    void cpfDuplicadoRetorna409() throws Exception {
        String admin = adminToken();
        String cpf = String.valueOf(System.nanoTime()).substring(0, 11);
        created("/api/clients", admin, Map.of("name", "A", "cpf", cpf));
        send("POST", "/api/clients", admin, Map.of("name", "B", "cpf", cpf))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------ fluxo de faturamento

    private JsonNode novaOrdem(String token, String mechanicId) throws Exception {
        JsonNode client = created("/api/clients", token, Map.of("name", "Cliente OS"));
        JsonNode vehicle = created("/api/vehicles", token, Map.of("plate", uniquePlate(), "brand", "Fiat",
                "model", "Uno", "year", 2012, "clientId", client.get("id").asText()));
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("clientId", client.get("id").asText());
        body.put("vehicleId", vehicle.get("id").asText());
        body.put("reportedProblem", "Barulho no motor");
        if (mechanicId != null) {
            body.put("mechanicId", mechanicId);
        }
        return created("/api/service-orders", token, body);
    }

    private JsonNode novaPeca(String token, int stock, String price) throws Exception {
        return created("/api/inventory/parts", token, Map.of("name", "Pastilha", "code", "P-" + UUID.randomUUID(),
                "purchasePrice", "10.00", "salePrice", price, "stockQuantity", stock));
    }

    @Test
    void faturamentoBaixaEstoqueSomaTotalEEstornaNaRemocao() throws Exception {
        String admin = adminToken();
        JsonNode order = novaOrdem(admin, null);
        JsonNode part = novaPeca(admin, 10, "50.00");
        String orderId = order.get("id").asText();
        String partId = part.get("id").asText();

        org.junit.jupiter.api.Assertions.assertTrue(order.get("orderNumber").asText().matches("OS\\d{4}\\d{6}"));

        JsonNode withItem = json.readTree(send("POST", "/api/service-orders/" + orderId + "/items", admin,
                        Map.of("partId", partId, "quantity", 3))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partsAmount", amount("150.00")))
                .andExpect(jsonPath("$.totalAmount", amount("150.00")))
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andReturn().getResponse().getContentAsString());
        String itemId = withItem.get("items").get(0).get("id").asText();

        send("GET", "/api/inventory/parts/" + partId, admin, null)
                .andExpect(jsonPath("$.stockQuantity", is(7)));

        send("PATCH", "/api/service-orders/" + orderId + "/labor", admin, Map.of("laborAmount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborAmount", amount("100.00")))
                .andExpect(jsonPath("$.totalAmount", amount("250.00")));

        send("DELETE", "/api/service-orders/" + orderId + "/items/" + itemId, admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partsAmount", amount("0")))
                .andExpect(jsonPath("$.totalAmount", amount("100.00")));

        send("GET", "/api/inventory/parts/" + partId, admin, null)
                .andExpect(jsonPath("$.stockQuantity", is(10)));
        send("GET", "/api/inventory/parts/" + partId + "/movements", admin, null)
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void estoqueInsuficienteRetorna400SemAlterarSaldo() throws Exception {
        String admin = adminToken();
        JsonNode order = novaOrdem(admin, null);
        JsonNode part = novaPeca(admin, 2, "30.00");

        send("POST", "/api/service-orders/" + order.get("id").asText() + "/items", admin,
                        Map.of("partId", part.get("id").asText(), "quantity", 5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Estoque insuficiente. Disponível: 2")));

        send("GET", "/api/inventory/parts/" + part.get("id").asText(), admin, null)
                .andExpect(jsonPath("$.stockQuantity", is(2)));
    }

    @Test
    void transicoesDeStatusSaoValidadas() throws Exception {
        String admin = adminToken();
        String id = novaOrdem(admin, null).get("id").asText();

        send("PATCH", "/api/service-orders/" + id + "/status", admin, Map.of("status", "FINALIZADA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Transição de status inválida de CRIADA para FINALIZADA")));

        send("PATCH", "/api/service-orders/" + id + "/status", admin,
                        Map.of("status", "EM_DIAGNOSTICO", "diagnosis", "Pastilhas gastas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis", is("Pastilhas gastas")));
    }

    @Test
    void ordemFinalizadaOuCanceladaNaoAceitaFaturamento() throws Exception {
        String admin = adminToken();
        String id = novaOrdem(admin, null).get("id").asText();
        JsonNode part = novaPeca(admin, 5, "10.00");

        send("PATCH", "/api/service-orders/" + id + "/status", admin, Map.of("status", "CANCELADA"))
                .andExpect(status().isOk());
        send("POST", "/api/service-orders/" + id + "/items", admin,
                        Map.of("partId", part.get("id").asText(), "quantity", 1))
                .andExpect(status().isBadRequest());
        send("PATCH", "/api/service-orders/" + id + "/labor", admin, Map.of("laborAmount", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mecanicoVinculadoAcessaApenasAPropriaOrdem() throws Exception {
        String admin = adminToken();
        User mecUser = createUser(Role.MECHANIC);
        JsonNode mechanic = created("/api/mechanics", admin,
                Map.of("name", "Zé", "userId", mecUser.getId().toString()));
        JsonNode minha = novaOrdem(admin, mechanic.get("id").asText());
        JsonNode outra = novaOrdem(admin, null);
        String token = tokenFor(mecUser);

        send("GET", "/api/service-orders/" + minha.get("id").asText(), token, null).andExpect(status().isOk());
        send("GET", "/api/service-orders/" + outra.get("id").asText(), token, null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Esta ordem de serviço não está atribuída a você")));
        send("GET", "/api/service-orders/mine", token, null)
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // ------------------------------------------------------------------ orçamentos e dashboard

    @Test
    void orcamentoSoPodeSerDecididoUmaVez() throws Exception {
        String admin = adminToken();
        JsonNode client = created("/api/clients", admin, Map.of("name", "Cliente Orç"));
        JsonNode vehicle = created("/api/vehicles", admin, Map.of("plate", uniquePlate(), "brand", "Fiat",
                "model", "Palio", "year", 2014, "clientId", client.get("id").asText()));
        JsonNode budget = created("/api/budgets", admin, Map.of("clientId", client.get("id").asText(),
                "vehicleId", vehicle.get("id").asText(), "totalAmount", "480.90"));
        String id = budget.get("id").asText();
        org.junit.jupiter.api.Assertions.assertTrue(budget.get("budgetNumber").asText().matches("BUD\\d{4}\\d{6}"));

        send("PATCH", "/api/budgets/" + id + "/approve", admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APROVADO")));
        send("PATCH", "/api/budgets/" + id + "/reject", admin, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Apenas orçamentos pendentes podem ser recusados")));
    }

    @Test
    void dashboardDevolveSeisMesesDeGrafico() throws Exception {
        String admin = adminToken();
        send("GET", "/api/dashboard/stats", admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRevenueChart.length()", is(6)))
                .andExpect(jsonPath("$.totalClients").isNumber());
        send("GET", "/api/dashboard/stats", tokenFor(createUser(Role.RECEPTIONIST)), null)
                .andExpect(status().isForbidden());
    }

    @Test
    void healthEhPublico() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database", is("up")));
    }

    // ------------------------------------------------------------------ interface web

    @Test
    void paginaDeLoginEhPublicaEAreaRestritaRedireciona() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/clients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/login")));
    }
}
