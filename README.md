# Carteira de Clientes Seguradora

Sistema full stack para gestao de carteira de seguros, com cadastro de clientes pessoa fisica e juridica, seguradoras e apolices. O projeto foi pensado para uma corretora ou profissional de seguros acompanhar vigencia, vencimentos, renovacoes e status da carteira em uma interface simples de operar.

> Os dados incluidos nas migrations sao ficticios e servem apenas para demonstracao, testes locais e portfolio.

## Problema Resolvido

Profissionais de seguros lidam com informacoes dispersas sobre clientes, seguradoras, vencimentos e renovacoes. Sem uma visao centralizada, fica facil perder apolices proximas do vencimento, consultar clientes com lentidao ou manter dados duplicados.

Este projeto resolve esse fluxo centralizando a operacao em uma API REST com regras de negocio e uma interface web responsiva para consulta, cadastro, filtros e acompanhamento dos principais indicadores da carteira.

## Funcionalidades

- Login com Basic Auth e perfis de administrador e leitura.
- Dashboard com totais de clientes, seguradoras, apolices vigentes, vencidas e a vencer.
- CRUD de clientes, seguradoras e apolices.
- Suporte a clientes pessoa fisica e pessoa juridica.
- Validacao de CPF, CNPJ, datas obrigatorias, duplicidades e vinculos.
- Tipos de seguro: auto, residencial, empresarial, vida, saude, viagem e outros.
- Status visual de apolices vigentes, vencidas, canceladas e proximas do vencimento.
- Recalculo automatico de status vigente/vencida, preservando apolices canceladas.
- Busca de clientes por nome/documento.
- Busca de apolices por numero, cliente, seguradora ou observacao.
- Filtros de apolices por status, tipo, cliente, seguradora e periodo de vencimento.
- Destaque para apolices vencendo nos proximos 30 dias.
- Mensagens amigaveis, estados de carregamento, estados vazios e confirmacao antes de excluir.
- API paginada com `Pageable`.
- Tratamento global de erros com respostas JSON padronizadas.
- Swagger UI com esquema Basic Auth.
- Migrations Flyway com constraints, indices e dados ficticios de demonstracao.

## Tecnologias Utilizadas

### Back-end

- Java 17
- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Flyway
- Springdoc OpenAPI / Swagger UI
- JUnit 5
- H2 em testes
- Maven

### Front-end

- React 18
- Vite 7
- TypeScript
- Lucide React
- CSS responsivo sem framework visual externo

### Infra local

- Docker Compose
- PostgreSQL 16

## Arquitetura do Projeto

```text
carteira-clientes-seguradora/
|-- docker-compose.yml
|-- pom.xml
|-- README.md
|-- frontend/
|   |-- src/
|   |   |-- App.tsx
|   |   |-- services/
|   |   |   `-- api.ts
|   |   |-- styles.css
|   |   `-- types.ts
|   |-- package.json
|   `-- vite.config.ts
`-- src/
    |-- main/
    |   |-- java/com/portfolio/insurance/
    |   |   |-- config/
    |   |   |-- controller/
    |   |   |-- domain/
    |   |   |-- dto/
    |   |   |-- exception/
    |   |   |-- mapper/
    |   |   |-- repository/
    |   |   |   `-- spec/
    |   |   |-- service/
    |   |   `-- validation/
    |   `-- resources/
    |       |-- application.yml
    |       `-- db/migration/
    `-- test/
```

### Visao em camadas

```text
React + Vite
    |
    | Basic Auth / HTTP
    v
Spring Boot REST API
    |
    | Services + regras de negocio
    v
Spring Data JPA + Specifications
    |
    v
PostgreSQL + Flyway
```

## Como Rodar o Back-end

### 1. Subir o PostgreSQL

```bash
docker compose up -d
```

Por padrao, o Postgres do Docker fica disponivel em:

```text
host: 127.0.0.1
port: 5433
database: insurance_db
user: postgres
password: postgres
```

A porta `5433` foi escolhida para evitar conflito com um PostgreSQL local na porta `5432`.

### 2. Rodar a API

```bash
mvn spring-boot:run
```

API local:

```text
http://localhost:8080
```

Se a porta `8080` ja estiver em uso, pare a instancia anterior com `Ctrl+C` ou configure outra porta para o Spring Boot.

## Como Rodar o Front-end

```bash
cd frontend
npm install
npm run dev
```

Front-end local:

```text
http://127.0.0.1:5173
```

Com o back-end rodando em `http://localhost:8080`, o Vite encaminha chamadas de `/api` para a API Spring Boot.

Para apontar o front para outra URL, crie um arquivo `frontend/.env` a partir de `frontend/.env.example`:

```text
VITE_API_BASE_URL=/api
```

Build de producao:

```bash
cd frontend
npm run build
```

## Configuracao do PostgreSQL

As variaveis abaixo podem ser sobrescritas no ambiente antes de iniciar o Docker ou a aplicacao:

```text
DB_HOST=127.0.0.1
DB_PORT=5433
DB_NAME=insurance_db
DB_USER=postgres
DB_PASS=postgres
```

Outras variaveis importantes:

```text
SPRING_PROFILES_ACTIVE=dev
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
APP_ADMIN_USER=admin
APP_ADMIN_PASSWORD=admin123
APP_READONLY_USER=user
APP_READONLY_PASSWORD=user123
```

Para verificar se o banco subiu:

```bash
docker compose ps
docker compose exec postgres psql -U postgres -d postgres -c "\l"
```

## Migrations

O projeto usa Flyway e executa as migrations automaticamente ao iniciar o back-end.

Migrations atuais:

- `V1__create_tables.sql`
- `V2__seed_data.sql`
- `V3__improve_constraints_indexes_and_seed.sql`
- `V4__customer_type_and_demo_seed.sql`

As migrations criam tabelas, constraints, indices e dados ficticios de demonstracao, incluindo clientes PF/PJ, seguradoras e apolices vigentes, vencidas e proximas do vencimento.

Para recriar o banco em ambiente local, remova o volume do Docker e suba novamente:

```bash
docker compose down -v
docker compose up -d
mvn spring-boot:run
```

## Swagger

Com o back-end rodando, acesse:

```text
http://localhost:8080/swagger-ui.html
```

Use uma das credenciais padrao de desenvolvimento para autenticar as chamadas.

## Credenciais Padrao

| Usuario | Senha | Perfil |
| --- | --- | --- |
| `admin` | `admin123` | Administrador, leitura e escrita |
| `user` | `user123` | Somente leitura |

> Para uso real, sobrescreva as senhas por variaveis de ambiente.

## Exemplos de Endpoints

### Clientes

```text
POST /customers
GET /customers
GET /customers/{id}
PUT /customers/{id}
DELETE /customers/{id}
```

Criar cliente pessoa fisica:

```bash
curl -X POST http://localhost:8080/customers \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Joao da Silva",
    "customerType": "PESSOA_FISICA",
    "cpf": "52998224725",
    "email": "joao@email.com",
    "phone": "+55 11 99999-0000",
    "birthDate": "1990-05-20"
  }'
```

Criar cliente pessoa juridica:

```bash
curl -X POST http://localhost:8080/customers \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Empresa Ficticia Ltda",
    "customerType": "PESSOA_JURIDICA",
    "cnpj": "12345678000195",
    "email": "contato@empresa.example.test",
    "phone": "+55 11 91000-0000"
  }'
```

### Seguradoras

```text
POST /insurers
GET /insurers
GET /insurers/{id}
PUT /insurers/{id}
DELETE /insurers/{id}
```

### Apolices

```text
POST /policies
GET /policies
GET /policies/{id}
PUT /policies/{id}
DELETE /policies/{id}
GET /policies/expiring?days=30
POST /policies/recalculate-status
```

Criar apolice:

```bash
curl -X POST http://localhost:8080/policies \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "policyNumber": "POL-2024-0100",
    "type": "AUTO",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "monthlyPremium": 120.50,
    "notes": "Apolice anual",
    "customerId": "33333333-3333-3333-3333-333333333333",
    "insurerId": "11111111-1111-1111-1111-111111111111"
  }'
```

O campo `status` pode ser omitido. O sistema calcula `VIGENTE` ou `VENCIDA` com base na data final. Quando `CANCELADA` for informado, o status e preservado.

### Filtros de apolices

```text
GET /policies?status=VIGENTE&type=AUTO
GET /policies?insurerId={uuid}&customerId={uuid}
GET /policies?startDateFrom=2024-01-01&startDateTo=2024-12-31
GET /policies?endDateFrom=2024-01-01&endDateTo=2024-12-31
GET /policies?search=POL-2024
GET /policies?search=Nome%20do%20cliente
GET /policies?search=Nome%20da%20seguradora
```

## Prints

Secao preparada para imagens do projeto:

| Tela | Print |
| --- | --- |
| Login | Adicionar print da tela de login |
| Dashboard | Adicionar print com cards e apolices a vencer |
| Clientes | Adicionar print da listagem e formulario |
| Apolices | Adicionar print dos filtros e detalhe lateral |

Sugestao de pasta para assets:

```text
docs/screenshots/
```

## Testes

Back-end:

```bash
mvn test
```

Front-end:

```bash
cd frontend
npm run build
```

## Melhorias Futuras

- Autenticacao com JWT e endpoint dedicado de login.
- Controle de permissoes mais granular por recurso.
- Upload de documentos das apolices.
- Alertas por email ou notificacoes para apolices proximas do vencimento.
- Historico de renovacoes e alteracoes de apolice.
- Exportacao da carteira para CSV/XLSX.
- Paginacao e ordenacao avancadas no front-end.
- Testes end-to-end cobrindo os fluxos principais.
- Deploy com Dockerfile para back-end e front-end.

## Observacoes

- Os dados de exemplo sao ficticios e nao representam clientes, empresas ou apolices reais.
- O front-end nao usa dados mockados; ele consome a API Spring Boot.
- O banco local e gerenciado pelo Docker Compose, enquanto o versionamento do schema fica nas migrations Flyway.
