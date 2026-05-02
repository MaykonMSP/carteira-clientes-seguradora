# Gestao de Carteira de Seguros

Projeto back-end em **Java 17 + Spring Boot 3** para cadastro e controle de clientes, seguradoras e apolices, com foco em regras de negocio de vigencia, vencimento, renovacao e API REST.

## Funcionalidades
- CRUD de seguradoras, clientes e apolices
- Filtros de apolices por status, tipo, seguradora, cliente, numero, periodo de inicio e periodo de vencimento
- Recalculo automatico de status vigente/vencida, preservando apolices canceladas
- Listagem de apolices a vencer nos proximos N dias
- Validacao real de CPF/CNPJ, datas, duplicidades e vinculos
- Paginacao com `Pageable`
- Tratamento global de erros com respostas JSON padronizadas
- Swagger UI com esquema Basic Auth
- Autenticacao Basic Auth com credenciais configuraveis por ambiente
- Migrations com Flyway, constraints, indices e dados ficticios de demonstracao

## Stack
- Java 17
- Spring Boot 3
- Spring Web + Spring Data JPA
- PostgreSQL
- Flyway
- Springdoc OpenAPI
- Spring Security
- JUnit 5 + Spring Boot Test

## Estrutura de pastas
```text
src/main/java/com/portfolio/insurance
|-- config
|-- controller
|-- domain
|-- dto
|-- exception
|-- mapper
|-- repository
|   `-- spec
|-- service
`-- validation
```

## Como rodar localmente
```bash
docker compose up -d
mvn spring-boot:run
```

Se aparecer `FATAL: banco de dados "insurance_db" nao existe`, confirme que o projeto esta usando o Postgres do Docker:

```bash
docker compose ps
docker compose exec postgres psql -U postgres -d postgres -c "\l"
```

Por padrao, o Postgres do Docker fica exposto em `127.0.0.1:5433` para evitar conflito com outro PostgreSQL local na porta `5432`.

As migrations criam dados ficticios para demonstracao, incluindo seguradoras, clientes pessoa fisica e juridica, apolices vigentes, vencidas e proximas do vencimento, com seguros auto, residencial, empresarial, vida e saude.

## Front-end
O front-end fica em `/frontend` e usa React, Vite e TypeScript. Com o back-end rodando em `http://localhost:8080`, o Vite encaminha `/api` para a API Spring Boot.
As telas consomem os dados reais da API, que por sua vez usa o PostgreSQL configurado em `DB_HOST`, `DB_PORT` e `DB_NAME`.

```bash
cd frontend
npm install
npm run dev
```

Para validar o build de producao:

```bash
npm run build
```

## Variaveis de ambiente
```text
DB_HOST=127.0.0.1
DB_PORT=5433
DB_NAME=insurance_db
DB_USER=postgres
DB_PASS=postgres

APP_ADMIN_USER=admin
APP_ADMIN_PASSWORD=admin123
APP_READONLY_USER=user
APP_READONLY_PASSWORD=user123
SPRING_PROFILES_ACTIVE=dev
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

## Swagger
- `http://localhost:8080/swagger-ui.html`

## Credenciais padrao
- `admin/admin123` com `ROLE_ADMIN`
- `user/user123` com `ROLE_USER`

> Para uso real, sobrescreva as senhas via variaveis de ambiente.

## Endpoints principais
- `POST /insurers`
- `GET /insurers`
- `GET /insurers/{id}`
- `PUT /insurers/{id}`
- `DELETE /insurers/{id}`
- `POST /customers`
- `GET /customers`
- `GET /customers/{id}`
- `PUT /customers/{id}`
- `DELETE /customers/{id}`
- `POST /policies`
- `GET /policies`
- `GET /policies/{id}`
- `PUT /policies/{id}`
- `DELETE /policies/{id}`
- `GET /policies/expiring?days=30`
- `POST /policies/recalculate-status`

## Filtros de apolices
```text
GET /policies?status=VIGENTE&type=AUTO&insurerId={uuid}&customerId={uuid}
GET /policies?startDateFrom=2024-01-01&startDateTo=2024-12-31
GET /policies?endDateFrom=2024-01-01&endDateTo=2024-12-31
GET /policies?search=POL-2024
```

## Exemplos
### Criar cliente
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

### Criar cliente pessoa juridica
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

### Criar apolice
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
