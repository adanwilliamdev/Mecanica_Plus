# 🔧 Torque Gestão

<p align="center">
  <strong>Sistema de gestão para oficinas mecânicas desenvolvido 100% em Java.</strong>
  <br>
  Gestão de clientes, veículos, ordens de serviço, faturamento, estoque e equipe em uma única plataforma.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-Optional-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
</p>

## 📌 Sobre o projeto

O **Torque Gestão** é uma aplicação web para gerenciamento de oficinas mecânicas, desenvolvida com Java 21 e Spring Boot. A plataforma centraliza as principais operações de uma oficina, desde o cadastro de clientes e veículos até o acompanhamento de serviços, controle de peças e cálculo do faturamento.

O sistema implementa um fluxo estruturado de ordens de serviço, com etapas controladas, orçamentos sujeitos à aprovação, lançamento de peças e mão de obra e atualização automática dos valores. O gerenciamento de estoque utiliza movimentações rastreáveis e controle de concorrência para proteger a integridade dos saldos.

A aplicação também conta com autenticação e autorização por perfil, interface web renderizada com Thymeleaf e API REST protegida por JWT. O PostgreSQL armazena os dados, o Flyway gerencia as migrações e o Redis pode ser utilizado para funcionalidades auxiliares, sem ser obrigatório para a execução do sistema.

O objetivo do projeto é demonstrar a aplicação prática de **desenvolvimento backend com Java, regras de negócio, segurança, persistência relacional, transações e arquitetura em camadas** em um cenário próximo das necessidades reais de uma oficina.

## ✨ Funcionalidades

- 👥 **Gestão de clientes:** cadastro e manutenção de informações.
- 🚗 **Gestão de veículos:** associação de veículos aos respectivos clientes.
- 🔧 **Ordens de serviço:** abertura, atribuição a mecânicos e acompanhamento de status.
- 🧾 **Orçamentos:** elaboração, aprovação e recusa de serviços propostos.
- 💰 **Faturamento:** cálculo automático dos valores de peças e mão de obra.
- 📦 **Controle de estoque:** entradas, saídas e atualização dos saldos.
- 👨‍🔧 **Gestão de mecânicos:** consulta da equipe e controle das ordens atribuídas.
- 📊 **Dashboard gerencial:** acompanhamento das informações operacionais.
- 🔐 **Controle de acesso:** permissões específicas para administradores, gerentes, recepcionistas e mecânicos.
- 🔑 **Autenticação segura:** sessões com proteção CSRF na interface web e JWT com refresh token rotativo na API.
- 📚 **API documentada:** documentação OpenAPI e Swagger UI.
- 🐳 **Ambiente Dockerizado:** inicialização simplificada da aplicação e de suas dependências.

## 🛠️ Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 |
| Web | Spring MVC e Thymeleaf |
| Interface | HTML, CSS próprio e JavaScript |
| API REST | Spring MVC e springdoc-openapi |
| Persistência | Spring Data JPA e Hibernate |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Segurança | Spring Security, JWT, refresh tokens e CSRF |
| Cache e rate limit | Redis opcional |
| Build | Maven |
| Testes unitários | JUnit 5 |
| Testes de integração | Testcontainers |
| Infraestrutura | Docker e Docker Compose |

## 🏗️ Arquitetura

A aplicação segue uma arquitetura em camadas, separando os componentes de apresentação, endpoints, regras de negócio, persistência e segurança.

```text
                  ┌─────────────────────────┐
                  │       Interface Web     │
                  │        Thymeleaf        │
                  └────────────┬────────────┘
                               │
                  ┌────────────▼────────────┐
                  │      Controllers        │
                  │      Web / REST         │
                  └────────────┬────────────┘
                               │
                  ┌────────────▼────────────┐
                  │        Services         │
                  │ Regras de negócio       │
                  └────────────┬────────────┘
                               │
                  ┌────────────▼────────────┐
                  │      Repositories       │
                  │    Spring Data JPA      │
                  └────────────┬────────────┘
                               │
                  ┌────────────▼────────────┐
                  │       PostgreSQL        │
                  │   Persistência / Dados  │
                  └─────────────────────────┘

       Segurança: Spring Security + JWT + CSRF
       Migrações: Flyway
       Cache / Rate Limit: Redis (opcional)
```

## 🚀 Como executar

### Pré-requisitos

- JDK 21
- Maven 3.9 ou superior para execução local
- Docker e Docker Compose para execução conteinerizada
- PostgreSQL 16 para execução local
- Redis opcional

### Opção 1: Docker Compose

Na raiz do repositório, execute:

```bash
docker compose up --build
```

Acesse a aplicação em:

**http://localhost:8080**

Na primeira inicialização, o sistema cria um administrador inicial conforme as variáveis de configuração.

| Configuração | Valor padrão |
|---|---|
| E-mail | `admin@mecanicaplus.com` |
| Senha | `admin123` |

> **Segurança:** essas credenciais são destinadas ao desenvolvimento. Altere a senha inicial por meio de `SEED_ADMIN_PASSWORD` e configure um segredo JWT forte antes de utilizar a aplicação em produção.

### Opção 2: execução local

Crie o banco de dados:

```bash
createdb mecanica
```

Configure as variáveis de ambiente conforme o arquivo `.env.example` e inicie a aplicação:

```bash
mvn spring-boot:run
```

As migrações do banco de dados são executadas automaticamente pelo Flyway durante a inicialização.

## 🌐 Endpoints e documentação

| Recurso | Endereço |
|---|---|
| Interface web | `/` |
| Login | `/login` |
| API REST | `/api/**` |
| Swagger UI | `/api/swagger-ui` |
| Especificação OpenAPI | `/api/openapi.json` |
| Health check | `/api/health` |

A API disponibiliza operações para autenticação, usuários, clientes, veículos, mecânicos, ordens de serviço, orçamentos e estoque, conforme os endpoints implementados no projeto.

## 👥 Perfis e permissões

O sistema utiliza controle de acesso baseado em perfis para restringir operações conforme a responsabilidade de cada usuário.

| Perfil | Permissões |
|---|---|
| `ADMIN` | Acesso completo ao sistema e gerenciamento de usuários |
| `MANAGER` | Dashboard, estoque, mecânicos, clientes, acompanhamento de ordens e aprovação ou recusa de orçamentos |
| `RECEPTIONIST` | Gestão de clientes e veículos, abertura de ordens e orçamentos e consulta de mecânicos |
| `MECHANIC` | Consulta e atualização das ordens atribuídas, incluindo lançamento de peças e mão de obra |

O autocadastro por `POST /api/auth/register` cria exclusivamente usuários com perfil `RECEPTIONIST`. A criação de usuários com outros perfis é restrita a administradores, por meio de `POST /api/auth/users`.

## 🔄 Regras de negócio

### Fluxo da ordem de serviço

As ordens de serviço seguem uma sequência controlada de estados:

```text
CRIADA
   ↓
EM_DIAGNOSTICO
   ↓
AGUARDANDO_APROVACAO
   ↓
APROVADA
   ↓
EM_EXECUCAO
   ↓
FINALIZADA
```

Qualquer etapa ainda aberta pode ser direcionada para `CANCELADA`, respeitando as regras implementadas. Não é permitido pular etapas do fluxo.

### Faturamento e peças

- A inclusão de peças em uma ordem de serviço reduz o estoque.
- O preço de venda da peça é registrado no momento de sua utilização.
- O total da ordem é recalculado conforme os lançamentos de peças e mão de obra.
- A remoção de uma peça estorna a quantidade correspondente ao estoque.
- Ordens finalizadas ou canceladas não permitem novas alterações de peças ou mão de obra.

### Integridade do estoque

O estoque é atualizado por meio de movimentações de entrada e saída. O controle de concorrência otimista impede que operações simultâneas sobrescrevam silenciosamente o saldo de uma mesma peça.

Quando ocorre um conflito de atualização, a operação concorrente pode receber uma resposta HTTP `409 Conflict`.

### Numeração e exclusões

- Ordens de serviço: `OSAAAA000001`.
- Orçamentos: `BUDAAAA000001`.
- A numeração é gerada por sequences do PostgreSQL.
- As exclusões são lógicas, mantendo os registros no banco com indicação de inatividade.

## 🔐 Segurança e configuração

As principais variáveis de ambiente estão documentadas em [`.env.example`](.env.example).

| Variável | Padrão | Finalidade |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/mecanica` | Conexão com o PostgreSQL |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `REDIS_HOST` | `localhost` | Endereço do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `JWT_SECRET` | Placeholder | Segredo usado na assinatura de tokens |
| `ENVIRONMENT` | `development` | Ambiente de execução |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080` | Origens permitidas para a API |
| `SEED_ADMIN_EMAIL` | `admin@mecanicaplus.com` | E-mail do administrador inicial |
| `SEED_ADMIN_PASSWORD` | `admin123` | Senha inicial do administrador |
| `APP_TIMEZONE` | `America/Sao_Paulo` | Fuso horário da aplicação |

Em produção, configure `ENVIRONMENT=production` e um `JWT_SECRET` forte, com pelo menos 32 caracteres. O administrador inicial só é criado nesse ambiente quando `SEED_ADMIN_PASSWORD` é diferente da senha padrão.

Não utilize as credenciais de desenvolvimento em ambientes públicos.

## 🧪 Testes

O projeto possui testes unitários e testes de integração.

**Testes unitários:**

```bash
mvn test
```

**Testes de integração:**

```bash
mvn verify
```

Os testes de integração utilizam Testcontainers com PostgreSQL e exigem Docker. Os testes unitários não exigem Docker, conforme a configuração do projeto.

## 📁 Estrutura do projeto

```text
torque-gestao/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── src/
    └── main/
        ├── java/com/mecanicaplus/
        │   ├── api/          # Controllers REST e tratamento de erros
        │   ├── web/          # Controllers e formulários Thymeleaf
        │   ├── service/      # Regras de negócio
        │   ├── repository/   # Spring Data JPA
        │   ├── domain/       # Entidades e enums
        │   ├── dto/          # Contratos da API
        │   ├── security/     # JWT, filtros e autorização
        │   ├── config/       # Configuração, segurança e seed
        │   └── exception/    # Exceções de negócio
        └── resources/
            ├── db/migration/ # Migrações Flyway
            ├── templates/    # Páginas Thymeleaf
            └── static/       # CSS e JavaScript
```

## 🎯 Objetivos técnicos

O Torque Gestão foi desenvolvido para aplicar e demonstrar conhecimentos em:

- Desenvolvimento backend com Java e Spring Boot.
- Construção de APIs REST e documentação OpenAPI.
- Autenticação, autorização e segurança de aplicações web.
- Modelagem de dados relacionais e migrações versionadas.
- Implementação de regras de negócio e fluxos de estados.
- Controle de concorrência e integridade de estoque.
- Testes unitários e de integração com Testcontainers.
- Conteinerização e configuração por variáveis de ambiente.

## 📄 Licença

Consulte a licença definida no repositório para obter informações sobre uso, distribuição e modificação do projeto.
