# 🔧 Mecânica+

### Sistema de gestão para oficinas mecânicas

Plataforma web para gerenciamento de **clientes, veículos, mecânicos, estoque, orçamentos e ordens de serviço**, com controle de acesso baseado em perfis.

## ✨ Funcionalidades

* 👥 Gestão de clientes e veículos
* 🔧 Gestão de mecânicos
* 📦 Controle de estoque de peças
* 📋 Orçamentos
* 🧾 Ordens de serviço
* 📊 Dashboard operacional
* 🔐 Autenticação com JWT e refresh token
* 👤 Controle de acesso por perfil
* 📝 Auditoria de movimentações de estoque
* ⚡ Cache e rate limiting com Redis
* 📱 Interface responsiva

## 🛠️ Stack

### Frontend

<p>
  <img src="https://img.shields.io/badge/Next.js%2016-000000?style=for-the-badge&logo=next.js&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Tailwind%20CSS%204-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" />
  <img src="https://img.shields.io/badge/shadcn%2Fui-000000?style=for-the-badge" />
  <img src="https://img.shields.io/badge/TanStack%20Query-FF4154?style=for-the-badge&logo=reactquery&logoColor=white" />
</p>

### Backend

<p>
  <img src="https://img.shields.io/badge/Python%203.12%2B-3776AB?style=for-the-badge&logo=python&logoColor=white" />
  <img src="https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white" />
  <img src="https://img.shields.io/badge/Pydantic-E92063?style=for-the-badge&logo=pydantic&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLAlchemy%202-D71F00?style=for-the-badge&logo=sqlalchemy&logoColor=white" />
  <img src="https://img.shields.io/badge/Alembic-2F2F2F?style=for-the-badge" />
</p>

### Infraestrutura

<p>
  <img src="https://img.shields.io/badge/PostgreSQL%2016-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis%207-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

## 🏗️ Arquitetura

```text
┌─────────────────────────────────────────────┐
│                  Frontend                   │
│ Next.js · TypeScript · Tailwind · shadcn   │
│             TanStack Query                  │
└──────────────────────┬──────────────────────┘
                       │
                 REST API · JSON
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                  Backend                    │
│ Python · FastAPI · Pydantic · SQLAlchemy   │
│                  Alembic                    │
└───────────────┬─────────────────┬───────────┘
                │                 │
                ▼                 ▼
        ┌──────────────┐   ┌──────────────┐
        │ PostgreSQL 16│   │   Redis 7    │
        │   Database   │   │ Cache / Rate │
        └──────────────┘   └──────────────┘
```

### Backend

* Rotas HTTP em `app/api`
* Regras de negócio em `app/services`
* Modelos em `app/models`
* Schemas Pydantic em `app/schemas`
* Configurações e segurança em `app/core`
* Persistência e conexão com banco em `app/db`
* Migrations com Alembic

### PostgreSQL

* Fonte principal de dados
* Sequences nativas para numeração de OS e orçamentos
* UUIDs nativos
* `timestamptz` para datas
* Controle de concorrência de estoque utilizando versão (`version`)

### Redis

* Cache do dashboard
* Rate limiting do login
* Serviço opcional em runtime
* A API continua funcionando caso o Redis fique indisponível

## 📁 Estrutura

```text
mecanica-plus/
├── backend/
│   ├── app/
│   │   ├── api/
│   │   ├── core/
│   │   ├── db/
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── services/
│   │   └── scripts/
│   ├── alembic/
│   └── tests/
│
├── frontend/
│   └── src/
│       ├── app/
│       ├── components/
│       ├── features/
│       ├── lib/
│       └── types/
│
└── docker-compose.yml
```

## 🚀 Como executar

### Docker

Suba toda a aplicação com:

```bash
docker compose up -d --build
```

Crie o usuário administrador:

```bash
docker compose exec api python -m app.scripts.seed
```

### Acessos

| Serviço | URL                                  |
| ------- | ------------------------------------ |
| Web     | http://localhost:3000                |
| API     | http://localhost:8080/api            |
| Swagger | http://localhost:8080/api/swagger-ui |

## 💻 Desenvolvimento

### Infraestrutura

```bash
docker compose up -d postgres redis
```

### Backend

Requisitos:

* Python 3.12+
* PostgreSQL 16
* Redis 7

```bash
cd backend

python -m venv .venv
```

Linux/macOS:

```bash
source .venv/bin/activate
```

Windows:

```powershell
.venv\Scripts\activate
```

Instale as dependências:

```bash
pip install -e ".[dev]"
```

Configure o ambiente:

```bash
cp .env.example .env
```

Execute as migrations:

```bash
alembic upgrade head
```

Crie o usuário inicial:

```bash
python -m app.scripts.seed
```

Inicie a API:

```bash
uvicorn app.main:app --reload --port 8080
```

### Frontend

Requisitos:

* Node.js 20.9+

```bash
cd frontend
```

Configure o ambiente:

```bash
cp .env.example .env.local
```

Instale as dependências:

```bash
npm install
```

Inicie o servidor:

```bash
npm run dev
```

## 👤 Perfis de acesso

| Perfil         | Permissões                                               |
| -------------- | -------------------------------------------------------- |
| `ADMIN`        | Acesso completo e gerenciamento de usuários              |
| `MANAGER`      | Dashboard, mecânicos, estoque, orçamentos e status de OS |
| `RECEPTIONIST` | Clientes, veículos, OS e orçamentos                      |
| `MECHANIC`     | Visualização e atualização das OS atribuídas             |

O cadastro público cria usuários com o perfil `RECEPTIONIST`.

As permissões são validadas diretamente na API através de `require_roles`. O frontend apenas oculta funcionalidades que o usuário não possui permissão para acessar.

## 🔐 Segurança

* JWT para autenticação
* Refresh tokens com rotação
* Refresh tokens armazenados no banco utilizando SHA-256
* Senhas protegidas com bcrypt
* Execução do bcrypt fora do event loop
* Rate limiting de login por IP e e-mail
* Contas desativadas têm acesso revogado imediatamente
* Alterações de perfil são aplicadas imediatamente
* Auditoria de estoque associada ao usuário autenticado
* Validação de `JWT_SECRET` em ambiente de produção
* CORS configurável por variável de ambiente

## 🧪 Testes

Os testes utilizam PostgreSQL real e fakeredis.

```bash
cd backend

pytest
```

Validação de código:

```bash
ruff check .
ruff format --check .
```

Frontend:

```bash
cd ../frontend

npm run typecheck
npm run lint
npm run build
```

### Banco de testes

A variável `TEST_DATABASE_URL` define o banco utilizado pelos testes.

Exemplo:

```text
postgresql+asyncpg://postgres:postgres@localhost:5432/mecanica_test
```

As migrations do Alembic são aplicadas automaticamente durante a execução dos testes.

## 🌱 Usuário de demonstração

O seed cria um usuário administrador para desenvolvimento:

```text
E-mail: admin@mecanicaplus.com
Senha: admin123
```

Para ambientes fora de desenvolvimento, defina:

```env
SEED_ADMIN_EMAIL=
SEED_ADMIN_PASSWORD=
```

## ⚙️ Variáveis de ambiente

### Backend

Arquivo:

```text
backend/.env.example
```

Principais variáveis:

```env
DATABASE_URL=
REDIS_URL=
JWT_SECRET=
JWT_EXPIRATION_MS=
REFRESH_TOKEN_DAYS=
CORS_ALLOWED_ORIGINS=
LOGIN_RATE_LIMIT_ATTEMPTS=
LOGIN_RATE_LIMIT_WINDOW_SECONDS=
DASHBOARD_CACHE_TTL_SECONDS=
```

### Frontend

Arquivo:

```text
frontend/.env.example
```

Variável principal:

```env
NEXT_PUBLIC_API_URL=
```

A URL deve incluir o prefixo `/api`.

## 📌 Observações

O dashboard possui algumas métricas estruturais preparadas para futuras implementações. Atualmente, determinados indicadores utilizam valores definidos no serviço de dashboard enquanto o modelo de faturamento e o vínculo entre ordens de serviço e peças utilizadas ainda estão sendo estruturados.

O token de acesso possui duração configurável através de `JWT_EXPIRATION_MS`. O uso de refresh tokens permite reduzir essa duração em ambientes de produção.

Atualmente, os tokens são armazenados em `localStorage`. Uma evolução futura seria utilizar cookies `HttpOnly` através de uma camada de autenticação no Next.js, reduzindo a exposição a ataques XSS.

## 📄 Licença

Este projeto está disponível para fins de estudo e portfólio.
