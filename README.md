# mercatus-customer-service

Microserviço dono dos dados de cliente da plataforma Mercatus: quem compra e para onde entregar. Mantém o cliente (e-mail único, status) e seus endereços, expondo a fonte de verdade por uma API REST versionada. O Order (fase futura) copia esses dados ao criar o pedido.

## Funcionalidades

- `Customer` com nome, e-mail (único, sem diferenciar maiúsculas), telefone e status `ACTIVE ⇄ INACTIVE`.
- `Address` estruturado (CEP, logradouro, número, complemento, bairro, cidade, UF, país `BR` fixo na v1), com no máximo um endereço padrão por cliente.
- Listagem com filtros (status, texto em nome/e-mail), paginação e ordenação.
- Optimistic locking (`@Version`) com conflito de versão → `409`.
- Erros em Problem Details (`400`/`404`/`409`/`422`), DTOs JSON, OpenAPI local, correlation id e e-mail mascarado nos logs.

## Stack

- Java 25, Spring Boot 4.1 (MVC), via `mercatus-parent:1.0.0`
- PostgreSQL + Flyway (`ddl-auto: validate`, schema é a fonte da verdade)
- springdoc-openapi
- Testcontainers, JUnit Jupiter 6, ArchUnit

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

Pré-requisitos: JDK 25, Maven 3.9+, PostgreSQL 17 para executar a aplicação e Docker acessível para os testes de integração. O parent `com.mercatus:mercatus-parent:1.0.0` deve estar instalado localmente ou acessível no GitHub Packages. Confira `mvn -version`: o Java usado pelo Maven deve ser 25; ajuste `JAVA_HOME` se necessário.

| Variável | Descrição | Padrão |
|---|---|---|
| `CUSTOMER_DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/customer` |
| `CUSTOMER_DB_USER` | Usuário do banco | `mercatus` |
| `CUSTOMER_DB_PASSWORD` | Senha do banco | `mercatus` |
| `SERVER_PORT` | Porta HTTP, sem sobrescrita por argumento | `8080` |
| `MAVEN_USERNAME` | Usuário do GitHub Packages | — |
| `MAVEN_PASSWORD` | Token do GitHub Packages | — |

### Rodar localmente

Na estrutura guarda-chuva, execute a partir deste repositório. Para instalar o parent localmente:

```bash
mvn -f ../mercatus-parent/pom.xml install
```

Para obter o parent remoto, configure `MAVEN_USERNAME` e `MAVEN_PASSWORD` e acrescente `-s .mvn/settings.xml` aos comandos Maven deste serviço. Esse settings não é carregado automaticamente. Mais detalhes no [README do parent](../mercatus-parent/README.md).

Com os padrões de conexão do Platform:

```bash
docker compose -f ../mercatus-platform/compose/docker-compose.yml --profile core up -d
docker compose -f ../mercatus-platform/compose/docker-compose.yml --profile core ps
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

Aguarde o PostgreSQL ficar saudável. O initdb cria `customer` somente em volume novo; para volumes existentes ou configuração personalizada, consulte o [README do Platform](../mercatus-platform/README.md). O Flyway aplica `V1__customer.sql` no start e o Hibernate valida o schema.

O exemplo usa a porta `8082` para permitir executar outros serviços ao mesmo tempo. Sem o argumento, o padrão é `8080`.

- Swagger UI: http://localhost:8082/api/v1/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8082/api/v1/v3/api-docs
- Health: http://localhost:8082/api/v1/actuator/health
- Ping: http://localhost:8082/api/v1/ping

O perfil opcional `local` desativa logs JSON. Para usá-lo, acrescente `-Dspring-boot.run.profiles=local` ao comando Maven. Por padrão, os logs usam ECS e o filtro recebe ou gera `X-Correlation-Id`, devolvendo-o na resposta.

## Endpoints (prefixo `/api/v1`)

- `POST /customers`, `GET /customers/{id}`, `GET /customers`, `PUT /customers/{id}`
- `POST /customers/{id}:activate`, `POST /customers/{id}:deactivate`
- `POST /customers/{id}/addresses`, `GET /customers/{id}/addresses`
- `PUT|DELETE /customers/{id}/addresses/{addressId}`, `PUT /customers/{id}/addresses/{addressId}/default`

Listagens de clientes aceitam `status`, `search` (nome/e-mail), `page=0`, `size=20` e `sort=name,asc` por padrão. A resposta paginada contém `content`, `page`, `size` e `totalElements`. Endereços são retornados como lista, sem paginação.

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8082/api/v1/customers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Cliente Exemplo","email":"cliente@example.com","phone":"11999999999"}'
```

Para adicionar endereço, envie `zipCode`, `street`, `number`, `district`, `city`, `state`, `complement` opcional e `asDefault` para indicar o padrão. O país é definido como `BR` pelo domínio. Os contratos completos ficam no OpenAPI gerado pela aplicação. As operações ainda não exigem autenticação.

## Modelagem e regras

- `Customer` é a raiz de agregado e contém seus `Address`; a regra de "no máximo um endereço padrão" é mantida pelo agregado.
- A unicidade de e-mail entre clientes é invariante entre agregados, garantida no banco (índice único em `LOWER(email)`) — não no agregado, que só conhece a si mesmo. E-mail duplicado, inclusive ignorando maiúsculas, retorna `409`.
- O endereço padrão único no banco usa `EXCLUDE (customer_id WITH =) WHERE (is_default) DEFERRABLE INITIALLY DEFERRED`: a verificação acontece no commit, permitindo trocar o padrão (desmarcar o antigo e marcar o novo) no mesmo flush.
- País fixo `BR`; UF normalizada para maiúsculas e validada com dois caracteres.
- `PUT /customers/{id}` recebe `version` do cliente e retorna `409` se divergir da versão atual. Ações de status e operações de endereço não recebem `version` no corpo; continuam sujeitas ao controle de concorrência da persistência do agregado.

## Testes

```bash
mvn test     # unitários (surefire): domínio + ArchUnit
mvn verify   # unitários + integração (failsafe, Testcontainers)
```

Cobrem: validação/normalização de e-mail, endereço padrão único, transições de status, mascaramento de e-mail; e, via Testcontainers, e-mail duplicado case-insensitive → 409, troca de endereço padrão, ativar/desativar, conflito de versão → 409, filtros + paginação e cliente inexistente → 404.

Os testes de integração iniciam PostgreSQL 17 em containers próprios; o Compose não precisa estar iniciado. `mvn test` não substitui a validação de banco de `mvn verify`, que exige Docker acessível.

## CI

O workflow `.github/workflows/ci.yml` executa em pushes e PRs para `main`, com JDK 25 e `mvn -B -s .mvn/settings.xml verify`. O token da execução precisa ter acesso de leitura ao pacote do parent.
