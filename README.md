# Clientes API

Microsserviço responsável pelo cadastro de clientes, consulta de score e troca de eventos via RabbitMQ.

## Decisões de arquitetura

A solução é uma aplicação Spring Boot única porque o domínio pedido tem um único contexto: clientes. PostgreSQL, Keycloak, RabbitMQ e o serviço de score são componentes externos e independentes.

```text
Cliente HTTP -> Clientes API -> PostgreSQL
                    |  |
                    |  +----> Serviço de Score (HTTP)
                    |
                    +-------> RabbitMQ
                              - publica CUSTOMER_CREATED
                              - consome CUSTOMER_STATUS_CHANGE

Keycloak ---------> validação OAuth 2.0/JWT na Clientes API
```

Dois mecanismos pequenos evitam inconsistências comuns em mensageria:

- A criação do cliente e o registro do evento acontecem na mesma transação. Um publicador lê a tabela `outbox_eventos` e só marca o evento como publicado depois do ACK do RabbitMQ.
- O consumidor grava o `eventId` em `mensagens_processadas`. Uma entrega repetida termina sem executar a alteração novamente.

O endpoint de score possui timeouts de conexão e leitura. Falhas do serviço externo são convertidas em respostas HTTP coerentes (`404`, `502` ou `504`). Erros da API seguem o formato `application/problem+json`.

## Tecnologias

- Java 25 (LTS)
- Spring Boot 3.5
- Maven
- PostgreSQL e Flyway
- Spring Security OAuth 2.0 Resource Server / JWT
- RabbitMQ
- Keycloak
- WireMock
- Docker Compose

## Executar com Docker

Requisito: Docker com o plugin Compose.

```bash
docker compose up --build
```

O PostgreSQL é exposto em `5433` por padrão para evitar conflito com instalações locais que normalmente usam `5432`. As portas externas podem ser alteradas sem editar o arquivo. Exemplo:

```bash
POSTGRES_PORT=5434 docker compose up --build
```

No PowerShell: `$env:POSTGRES_PORT=5434; docker compose up --build`.

Serviços disponíveis:

| Serviço | Endereço | Credenciais |
|---|---|---|
| Clientes API | http://localhost:8080 | JWT |
| Keycloak | http://localhost:8081 | `admin` / `admin` |
| RabbitMQ Management | http://localhost:15672 | `clientes` / `clientes` |
| WireMock (score) | http://localhost:8082 | - |
| PostgreSQL | `localhost:5433/clientes` | `clientes` / `clientes` |

Para remover também os dados persistidos:

```bash
docker compose down -v
```

## Obter um Access Token

O realm local já cria dois usuários:

| Usuário | Senha | Perfis |
|---|---|---|
| `usuario` | `usuario123` | USER |
| `administrador` | `admin123` | USER, ADMIN |

Token de administrador:

```bash
curl -s -X POST http://localhost:8081/realms/clientes/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=clientes-api" \
  -d "username=administrador" \
  -d "password=admin123" \
  -d "grant_type=password"
```

Copie o campo `access_token` da resposta. USER pode consultar; somente ADMIN pode criar, alterar ou excluir.

## Endpoints

| Método | Endpoint | Perfil | Descrição |
|---|---|---|---|
| POST | `/customers` | ADMIN | Cadastra um cliente |
| PUT | `/customers/{id}` | ADMIN | Altera nome, e-mail e status |
| DELETE | `/customers/{id}` | ADMIN | Exclui um cliente |
| GET | `/customers/{id}` | USER ou ADMIN | Consulta por id |
| GET | `/customers` | USER ou ADMIN | Lista paginada |
| GET | `/customers?status=ACTIVE` | USER ou ADMIN | Filtra por status |
| GET | `/customers/search?name=maria` | USER ou ADMIN | Busca até 20 clientes por nome |
| GET | `/customers/{id}/score` | USER ou ADMIN | Consulta o score pelo CPF |
| GET | `/actuator/health` | público | Health check |

Parâmetros de paginação aceitos: `page`, `size` e `sort`.

### Cadastrar um cliente

O CPF do exemplo é válido para permitir a validação completa.

```bash
curl -i -X POST http://localhost:8080/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria da Silva",
    "cpf": "52998224725",
    "email": "maria@email.com",
    "status": "ACTIVE"
  }'
```

### Consultar clientes

```bash
curl http://localhost:8080/customers?status=ACTIVE \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8080/customers/search?name=maria \
  -H "Authorization: Bearer $TOKEN"
```

### Consultar score

```bash
curl http://localhost:8080/customers/1/score \
  -H "Authorization: Bearer $TOKEN"
```

O WireMock responde com sucesso para CPFs de 11 dígitos. O CPF `11144477735` possui atraso proposital de 5 segundos para exercitar a resposta `504 Gateway Timeout` (é necessário cadastrá-lo em uma base limpa).

## RabbitMQ

Ao criar um cliente, a aplicação publica em:

- exchange: `clientes.eventos`
- routing key: `customer.created`
- fila de demonstração: `clientes.criado`

Exemplo do evento:

```json
{
  "eventId": "7ba85b17-7d33-4d17-9101-569a362239f2",
  "eventType": "CUSTOMER_CREATED",
  "customerId": 1,
  "createdAt": "2026-08-11T15:30:00Z"
}
```

Para solicitar mudança de status, publique no mesmo exchange com a routing key `customer.status.change`. Também é possível usar a aba **Exchanges** da interface do RabbitMQ.

```bash
curl -u clientes:clientes -X POST \
  http://localhost:15672/api/exchanges/%2F/clientes.eventos/publish \
  -H "Content-Type: application/json" \
  -d '{
    "properties": {"content_type": "application/json"},
    "routing_key": "customer.status.change",
    "payload": "{\"eventId\":\"cbca5352-22ad-48f2-aaf2-704735bc7737\",\"eventType\":\"CUSTOMER_STATUS_CHANGE\",\"customerId\":1,\"status\":\"INACTIVE\"}",
    "payload_encoding": "string"
  }'
```

Após três falhas, a mensagem vai para `clientes.status.alteracao.dlq`. Repetir o mesmo `eventId` não altera o cliente novamente.

## Executar sem container da aplicação

Pare um eventual container da API e suba apenas as dependências:

```bash
docker compose stop clientes-api
docker compose up -d postgres rabbitmq keycloak score
mvn spring-boot:run
```

Na IDE, execute `ClientesApplication` depois que as dependências estiverem disponíveis. A configuração padrão da aplicação local utiliza PostgreSQL em `localhost:5433`, RabbitMQ em `localhost:5672`, Keycloak em `localhost:8081` e o serviço de score em `localhost:8082`.

Se preferir manter o PostgreSQL do projeto em outra porta, informe a mesma porta ao Compose e ao Spring. Exemplo no PowerShell:

```powershell
$env:POSTGRES_PORT=5434
$env:DB_URL="jdbc:postgresql://localhost:5434/clientes"
docker compose up -d postgres rabbitmq keycloak score
mvn spring-boot:run
```

As configurações podem ser substituídas por variáveis de ambiente:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/clientes` |
| `DB_USER` / `DB_PASSWORD` | `clientes` / `clientes` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `clientes` / `clientes` |
| `SCORE_URL` | `http://localhost:8082` |
| `SCORE_CONNECT_TIMEOUT` | `2s` |
| `SCORE_READ_TIMEOUT` | `2s` |
| `JWT_ISSUER_URI` | `http://localhost:8081/realms/clientes` |
| `JWT_JWK_SET_URI` | endpoint de chaves do realm local |
| `APP_TIME_ZONE` | `America/Sao_Paulo` (Brasília, UTC-3) |

Datas são armazenadas no banco como instantes UTC e retornadas pela API no fuso configurado. Por padrão, uma data como `2026-11-11T15:30:00Z` é apresentada como `2026-11-11T12:30:00-03:00`.

## Testes

```bash
mvn test
```

Os testes cobrem regras de criação e atualização, CPF duplicado, cliente inexistente, idempotência, integração HTTP de score e matriz básica de autorização.

## Coleção Postman

A pasta `docs/postman` contém uma coleção Postman e um environment local prontos para importação. A coleção cobre obtenção dos tokens, health check, CRUD, filtros, score, RabbitMQ e cenários de erro.

Consulte `docs/README.md` para a ordem de execução e instruções de importação.
