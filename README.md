# mercatus-customer-service

Microserviço dono dos dados de cliente da plataforma Mercatus: quem compra e para onde entregar. Mantém o cliente (e-mail único, status) e seus endereços, expondo a fonte de verdade por uma API REST versionada. O Order (fase futura) copia esses dados ao criar o pedido.

## Funcionalidades

- `Customer` com nome, e-mail (único, sem diferenciar maiúsculas), telefone e status `ACTIVE ⇄ INACTIVE`.
- `Address` estruturado (CEP, logradouro, número, complemento, bairro, cidade, UF, país `BR` fixo na v1), com no máximo um endereço padrão por cliente.
- Listagem com filtros (status, texto em nome/e-mail), paginação e ordenação.
- Optimistic locking (`@Version`) com conflito de versão → `409`.
- Erros em Problem Details (`404`/`409`/`422`), DTOs JSON, OpenAPI local, correlation id e e-mail mascarado nos logs.

## Stack

- Java 25, Spring Boot 4 (MVC)
- PostgreSQL + Flyway (`ddl-auto: validate`, schema é a fonte da verdade)
- springdoc-openapi
- Testcontainers, JUnit 5, ArchUnit

## Estrutura

Arquitetura Hexagonal (Ports & Adapters), verificada por ArchUnit:

```
src/main/java/com/mercatus/customer/
├── domain/           # núcleo livre de frameworks: Customer (raiz), Address, Email, Status
├── application/      # casos de uso + portas (port/in, port/out)
├── infrastructure/   # adapters de saída: persistence (JPA)
└── web/              # adapters de entrada: controllers, DTOs, Problem Details, config
```

`web` depende das portas de entrada; os casos de uso orquestram o domínio e dependem da porta de saída `CustomerRepository`, implementada em `infrastructure`. O domínio não conhece nenhuma borda. ArchUnit garante: domínio sem dependência de outras camadas nem de frameworks; `application` só depende de `domain`; `@Entity` só em `infrastructure`; `@RestController` só em `web`.

## Configuração

Pré-requisitos: Java 25, Maven 3.9+, Docker, PostgreSQL 17 (via `mercatus-platform` ou Testcontainers) e acesso ao GitHub Packages para o `mercatus-parent`.

| Variável | Descrição | Padrão |
|---|---|---|
| `CUSTOMER_DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/customer` |
| `CUSTOMER_DB_USER` | Usuário do banco | `mercatus` |
| `CUSTOMER_DB_PASSWORD` | Senha do banco | `mercatus` |
| `MAVEN_USERNAME` | Usuário do GitHub Packages | — |
| `MAVEN_PASSWORD` | Token do GitHub Packages | — |

### Rodar localmente

```bash
# Postgres via mercatus-platform (perfil core); o banco "customer" é criado no primeiro boot pelo initdb
docker compose --profile core up -d

# Aplicação (Flyway aplica as migrations no start)
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v1/v3/api-docs
- Health: http://localhost:8080/api/v1/actuator/health

## Endpoints (prefixo `/api/v1`)

- `POST /customers`, `GET /customers/{id}`, `GET /customers`, `PUT /customers/{id}`
- `POST /customers/{id}:activate`, `POST /customers/{id}:deactivate`
- `POST /customers/{id}/addresses`, `GET /customers/{id}/addresses`
- `PUT|DELETE /customers/{id}/addresses/{addressId}`, `PUT /customers/{id}/addresses/{addressId}/default`

## Modelagem e regras

- `Customer` é a raiz de agregado e contém seus `Address`; a regra de "no máximo um endereço padrão" é mantida pelo agregado.
- A unicidade de e-mail entre clientes é invariante entre agregados, garantida no banco (índice único em `LOWER(email)`) — não no agregado, que só conhece a si mesmo. E-mail duplicado, inclusive ignorando maiúsculas, retorna `409`.
- O endereço padrão único no banco usa `EXCLUDE (customer_id WITH =) WHERE (is_default) DEFERRABLE INITIALLY DEFERRED`: a verificação acontece no commit, permitindo trocar o padrão (desmarcar o antigo e marcar o novo) no mesmo flush.
- País fixo `BR`; UF normalizada para maiúsculas; atualização exige `version` no corpo (conflito → `409`).

## Testes

```bash
mvn test     # unitários (surefire): domínio + ArchUnit
mvn verify   # unitários + integração (failsafe, Testcontainers)
```

Cobrem: validação/normalização de e-mail, endereço padrão único, transições de status, mascaramento de e-mail; e, via Testcontainers, e-mail duplicado case-insensitive → 409, troca de endereço padrão, ativar/desativar, conflito de versão → 409, filtros + paginação e cliente inexistente → 404.
