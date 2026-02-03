# Gestão de Carteira de Seguros (Apólices)

Projeto back-end em **Java 17 + Spring Boot 3** para cadastro e controle de clientes, seguradoras e apólices, com foco em regras de negócio (vigência, vencimento, renovação) e API REST.

## Contexto do problema
Empresas que gerenciam carteira de seguros precisam acompanhar clientes, seguradoras e apólices, além de identificar rapidamente apólices vencidas ou próximas da renovação. Este sistema entrega uma API REST completa para esse controle.

## Funcionalidades
- CRUD de **seguradoras**, **clientes** e **apólices**
- Filtros avançados de apólices (status, tipo, seguradora, cliente, período, número)
- Recalcular status automaticamente (vigente/vencida) e em lote
- Listar apólices a vencer nos próximos N dias
- Validações de CPF/CNPJ e datas
- Paginação com `Pageable`
- Swagger UI (OpenAPI)
- Autenticação Basic Auth
- Migrations com Flyway

## Stack
- Java 17
- Spring Boot 3
- Spring Web + Spring Data JPA
- PostgreSQL
- Flyway
- Springdoc OpenAPI
- Spring Security (Basic Auth)
- JUnit 5 + Spring Boot Test

## Estrutura de pastas
```
src/main/java/com/portfolio/insurance
├── config
├── controller
├── domain
├── dto
├── exception
├── mapper
├── repository
│   └── spec
└── service
```

## Como rodar localmente (Maven)
```bash
# subir banco
docker compose up -d

# rodar aplicação
./mvnw spring-boot:run
```

## Como rodar com Docker (somente Postgres)
```bash
docker compose up -d
```

## Variáveis de ambiente
```
DB_HOST=localhost
DB_NAME=insurance_db
DB_USER=postgres
DB_PASS=postgres
```

## Documentação Swagger
- `http://localhost:8080/swagger-ui.html`

## Credenciais (Basic Auth)
- **admin/admin123** (ROLE_ADMIN)
- **user/user123** (ROLE_USER)

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

## Exemplos de requests (curl)
### Criar cliente
```bash
curl -X POST http://localhost:8080/customers \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João da Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "phone": "+55 11 99999-0000",
    "birthDate": "1990-05-20"
  }'
```

### Criar apólice
```bash
curl -X POST http://localhost:8080/policies \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "policyNumber": "POL-2024-0100",
    "type": "AUTO",
    "status": "VIGENTE",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "monthlyPremium": 120.50,
    "notes": "Apólice anual",
    "customerId": "33333333-3333-3333-3333-333333333333",
    "insurerId": "11111111-1111-1111-1111-111111111111"
  }'
```

## Próximos passos
- Tela web para acompanhamento da carteira
- Exportação CSV/Excel
- Notificações de renovação por e-mail
- Integração com mensageria
