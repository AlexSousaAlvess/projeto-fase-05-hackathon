# SaúdePoints — Gamificação da Saúde Preventiva

Plataforma back-end que incentiva cidadãos do SUS a adotarem saúde preventiva por meio de pontos e recompensas.
Ações como vacinação e exames preventivos geram pontos que podem ser trocados por benefícios públicos (transporte, energia, cultura).

**FIAP PosTech — Hackathon Fase 5 | Arquitetura e Desenvolvimento Java**

---

## Serviços

| Serviço | Porta | Descrição |
| --- | --- | --- |
| `api-gateway` | 8080 | Ponto de entrada único — roteamento e circuit breaker |
| `discovery-server` | 8761 | Eureka — registro e descoberta de serviços |
| `citizen-service` | 8081 | Perfil de cidadãos; sincroniza com `sus-mock-service` |
| `health-action-service` | 8082 | Registro de ações de saúde (vacinação / exames) |
| `points-service` | 8083 | Saldo e transações de pontos |
| `sus-mock-service` | 8087 | Simula a base nacional de cidadãos (CADSUS) |

---

## Opção 1 — Docker Compose (recomendado)

Sobe toda a stack em containers: infraestrutura (Kafka, PostgreSQL) + todos os microsserviços.

### Pré-requisitos

- Docker e Docker Compose
- Java 21 e Maven (apenas para o build dos JARs)

### Passo a passo

```bash
# 1. Build de todos os módulos (gera os JARs que o Docker vai empacotar)
mvn clean package -DskipTests

# 2. Build das imagens e subida de toda a stack
docker compose up --build -d
```

Aguarde todos os serviços ficarem saudáveis (cerca de 60–90 segundos).
Acompanhe os logs:

```bash
docker compose logs -f
```

### Verificar se está tudo no ar

```bash
docker compose ps
```

Todos os serviços devem estar com status `running`. Acesse também o dashboard do Eureka:

```
http://localhost:8761
```

Todos os 4 serviços de domínio + gateway devem aparecer registrados.

### Parar a stack

```bash
docker compose down
```

Para remover também os volumes (dados dos bancos):

```bash
docker compose down -v
```

---

## Opção 2 — Maven + infraestrutura local

Útil para desenvolvimento: sobe só a infraestrutura via Docker e roda os serviços via Maven (com reload rápido).

### Pré-requisitos

- Java 21
- Maven
- Docker e Docker Compose

### Passo a passo

```bash
# 1. Sobe apenas Kafka e os bancos PostgreSQL
make infra-up

# 2. Em terminais separados (ou em segundo plano), sobe cada serviço
make run-discovery-server
make run-sus-mock-service
make run-citizen-service
make run-health-action-service
make run-points-service
make run-api-gateway
```

Ou sobe tudo de uma vez em segundo plano com logs em `logs/<serviço>.log`:

```bash
make start
```

Ver status e parar:

```bash
make status
make stop
```

---

## Testando o fluxo via Postman / Swagger

Importe a collection em `postman/saude-gamificada.postman_collection.json` ou acesse o Swagger UI de cada serviço:

| Serviço | Swagger UI |
| --- | --- |
| citizen-service | http://localhost:8081/swagger-ui.html |
| health-action-service | http://localhost:8082/swagger-ui.html |
| points-service | http://localhost:8083/swagger-ui.html |
| sus-mock-service | http://localhost:8087/swagger-ui.html |

### Fluxo de demonstração

```
1. GET  /sus/citizens              → lista cidadãos disponíveis no mock do SUS
2. POST /citizens/sync-sus         → sincroniza cidadãos para o citizen-service
3. GET  /citizens                  → confirma cidadãos importados
4. POST /health-actions            → registra vacinação (100 pts) ou exame (150 pts)
5. GET  /points/{citizenId}        → consulta saldo acumulado
```

---

## Documentação

| Documento | Conteúdo |
| --- | --- |
| `doc/relatorio-final.md` | Relatório completo do projeto |
| `doc/como-rodar.md` | Guia detalhado de execução com Make |
| `doc/diagramas/10-c4-model.md` | C4 Model completo (Context, Container, Component) |
| `doc/diagramas/` | Casos de uso, entidades, eventos Kafka, sequências |
| `doc/pitch.html` | Pitch de negócio (abrir no browser) |
| `doc/pitch-tecnico.html` | Pitch técnico — demo em slides (abrir no browser) |

## Stack

Java 21 · Spring Boot 3.x · Spring Cloud (Eureka, Gateway, OpenFeign) · Apache Kafka · PostgreSQL 16 · Resilience4j · Docker Compose · Maven multi-módulo · Arquitetura Hexagonal
