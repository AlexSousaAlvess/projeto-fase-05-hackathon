# Diagrama de Fluxo de Eventos (Event-Driven Architecture)

## Visão Geral do Barramento de Eventos

```mermaid
flowchart LR
    subgraph producers["Produtores"]
        HA["health-action-service"]
        PT["points-service"]
    end

    subgraph kafka["Apache Kafka — Tópicos"]
        T1(["health-action.registered"])
        T2(["points.credited"])
    end

    subgraph consumers["Consumidores"]
        PT2["points-service"]
    end

    HA -->|publica| T1
    PT -->|publica| T2

    T1 -->|consome| PT2
```

---

## Fluxo 0: Sincronização de Cidadãos com o SUS (mock)

```mermaid
flowchart TD
    A["Admin/Postman: POST /citizens/sync-sus"] --> B["citizen-service\nChama sus-mock-service via Feign\nGET /sus/citizens"]
    B --> C["sus-mock-service\nRetorna dataset fixo de cidadãos"]
    C --> D{"citizen-service\nPara cada cidadão: existsByCpf?"}
    D -->|"não existe"| E["Persiste novo CITIZEN (status ACTIVE)"]
    D -->|"já existe"| F["Ignora (skipped++)"]
    E --> G["Retorna { synced, skipped }"]
    F --> G
```

> Comunicação síncrona (REST/Feign), não Kafka — `sus-mock-service` simula um sistema externo (CADSUS).

---

## Fluxo 1: Registro de Ação de Saúde

```mermaid
flowchart TD
    A["Cidadão: POST /health-actions"] --> B["health-action-service\nSalva ação (status: VALIDATED)\nCalcula pontos"]
    B --> C["Publica\nhealth-action.registered\n{citizenId, actionType, pointsValue}"]
    B --> D["Retorna 201 Created\n{actionId, pointsValue}"]
    C --> E["points-service\nCria/atualiza PointsAccount\nCria PointTransaction (CREDIT)\nAtualiza saldo"]
    E --> F["Publica\npoints.credited\n{citizenId, amount, newBalance}"]
```

---

## Catálogo de Eventos (MVP)

| Tópico Kafka | Publicado por | Consumido por | Payload |
|---|---|---|---|
| `health-action.registered` | health-action-service | points-service | `{eventId, citizenId, actionId, actionType, pointsValue, registeredAt}` |
| `points.credited` | points-service | *(notification-service — roadmap)* | `{eventId, citizenId, amount, newBalance, createdAt}` |

> **`eventId`** é usado como chave de idempotência pelo `points-service` para evitar duplo crédito em caso de reentrega pelo Kafka.

---

## Roadmap — Eventos das próximas fases

| Tópico | Fase |
|---|---|
| `redemption.requested` / `points.debited` / `redemption.approved` / `reward.granted` | Fase 1 — `redemption-service` + `reward-service` |
| `points.credited` → `notification-service` | Fase 1 — `notification-service` |
