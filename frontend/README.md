# Front-end da Carteira de Seguros

SPA em React, Vite e TypeScript para operar a API Spring Boot do projeto.

## Como rodar

```bash
cd frontend
npm install
npm run dev
```

Com o back-end em `http://localhost:8080`, o Vite encaminha chamadas de `/api` para a API.

Para apontar para outra URL de API, crie um `.env` a partir do `.env.example`:

```text
VITE_API_BASE_URL=/api
```

Credenciais padrao do back-end:

- `admin/admin123`: leitura e escrita
- `user/user123`: leitura

## Integracao

O front-end nao usa dados mockados. Todas as telas consomem a API Spring Boot:

- `GET/POST/PUT/DELETE /customers`
- `GET/POST/PUT/DELETE /insurers`
- `GET/POST/PUT/DELETE /policies`
- `GET /policies/expiring`

A autenticacao atual usa Basic Auth para acompanhar o back-end existente. Uma evolucao natural seria JWT, com um endpoint de login que emite token, expiracao configuravel e troca do filtro Basic Auth no Spring Security.
