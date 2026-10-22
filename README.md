# Mecânica+

Sistema de gestão para oficinas mecânicas, 100% Java: clientes, veículos, mecânicos, ordens de
serviço com faturamento (peças e mão de obra), orçamentos, estoque e dashboard.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem / runtime | Java 21 |
| Framework | Spring Boot 3.3 (Web, Validation, Security, Data JPA) |
| Interface web | Thymeleaf (renderização no servidor) + CSS próprio |
| API REST | Spring MVC, documentada com springdoc-openapi |
| Banco de dados | PostgreSQL 16 + Flyway (migrações) |
| Cache / rate limit | Redis (opcional — sem ele a aplicação segue funcionando) |
| Autenticação | Sessão + CSRF na interface web; JWT + refresh token rotativo na API |
| Build / testes | Maven, JUnit 5, Testcontainers |

## Como executar

### Docker Compose (mais simples)

```bash
docker compose up --build
```

Acesse <http://localhost:8080>. O administrador inicial é criado na primeira inicialização:

- e-mail: `admin@mecanicaplus.com`
- senha: `admin123` (**altere** com `SEED_ADMIN_PASSWORD` fora de desenvolvimento)

### Local

Requisitos: JDK 21, Maven 3.9+, PostgreSQL 16 e (opcional) Redis.

```bash
createdb mecanica
mvn spring-boot:run
```

As variáveis de ambiente estão em [`.env.example`](.env.example). As migrações do Flyway rodam
automaticamente na inicialização.

## Endereços

| O quê | Onde |
|---|---|
| Interface web | `/` (login em `/login`) |
| API REST | `/api/**` |
| Swagger UI | `/api/swagger-ui` |
| OpenAPI (JSON) | `/api/openapi.json` |
| Health check | `/api/health` |

## Perfis e permissões

| Perfil | Pode |
|---|---|
| `ADMIN` | tudo |
| `MANAGER` | dashboard, estoque, mecânicos, aprovar/recusar orçamentos, acompanhar OS e clientes |
| `RECEPTIONIST` | clientes, veículos, abrir OS e orçamentos, consultar mecânicos |
| `MECHANIC` | apenas as OS atribuídas a ele: avançar status e lançar peças/mão de obra |

O autocadastro (`POST /api/auth/register`) sempre cria `RECEPTIONIST`. Outros perfis só podem ser
criados por um `ADMIN` em `POST /api/auth/users`.

## Regras de negócio

- **Fluxo da OS:** `CRIADA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO →
  FINALIZADA`; qualquer etapa aberta pode ir para `CANCELADA`. Não é permitido pular etapas.
- **Faturamento:** adicionar peça baixa o estoque (preço de venda travado no momento do uso) e
  recalcula o total; remover estorna ao estoque. OS finalizada ou cancelada não aceita mais
  alterações de peças ou mão de obra.
- **Estoque:** o saldo só muda por movimentações (entrada/saída) e é protegido por lock otimista:
  duas operações simultâneas na mesma peça não sobrescrevem o saldo (a segunda recebe conflito 409).
- **Numeração:** `OSAAAA000001` e `BUDAAAA000001`, geradas por sequences do PostgreSQL (atômicas).
- **Exclusões** são lógicas (registro marcado como inativo).

## Variáveis de ambiente principais

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/mecanica` / `postgres` / `postgres` | Banco |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis |
| `JWT_SECRET` | placeholder | **Obrigatório em produção**: 32+ caracteres |
| `ENVIRONMENT` | `development` | Com `production`, a aplicação recusa subir com segredo JWT fraco |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080` | Origens da API |
| `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` | `admin@mecanicaplus.com` / `admin123` | Admin inicial |
| `APP_TIMEZONE` | `America/Sao_Paulo` | Fuso para exibição de datas |

Em produção (`ENVIRONMENT=production`) o administrador inicial só é criado se
`SEED_ADMIN_PASSWORD` for diferente do padrão.

## Testes

```bash
mvn test      # testes unitários (não exigem Docker)
mvn verify    # + testes de integração (PostgreSQL via Testcontainers; exige Docker)
```

## Estrutura

```
src/main/java/com/mecanicaplus
├── api/          controllers REST e tratamento global de erros
├── web/          controllers e formulários da interface Thymeleaf
├── service/      regras de negócio
├── repository/   Spring Data JPA
├── domain/       entidades e enums
├── dto/          contratos da API
├── security/     JWT, filtros, autorização por perfil
├── config/       configuração, segurança, seed
└── exception/    exceções de negócio
src/main/resources
├── db/migration/ migrações Flyway
├── templates/    páginas Thymeleaf
└── static/       CSS e JS
```
