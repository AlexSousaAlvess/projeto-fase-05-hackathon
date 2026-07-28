# Diagrama de Entidades (Domain Model por Bounded Context)

> Cada bounded context representa o banco de dados de um microsserviço.  
> **Não há joins entre contextos** — cada serviço é dono exclusivo dos seus dados.

---

## Contexto: citizen-service

```mermaid
erDiagram
    CITIZEN {
        uuid id PK
        string cpf UK
        string name
        string email UK
        string phone
        date birth_date
        string status "ACTIVE | INACTIVE"
        timestamp created_at
        timestamp updated_at
    }
```

> Registros são criados via sincronização com `sus-mock-service` (simulação do CADSUS) — não há cadastro direto pelo cidadão. Ver `doc/diagramas/02-casos-de-uso.md` UC1.

---

## Contexto: health-action-service

```mermaid
erDiagram
    HEALTH_ACTION {
        uuid id PK
        uuid citizen_id "ref externo"
        string action_type "VACCINATION | PREVENTIVE_EXAM"
        string description
        string proof_document_url
        string status "PENDING | VALIDATED | REJECTED"
        int points_value
        timestamp registered_at
        timestamp validated_at
        string rejection_reason
    }
```

**Pontos por tipo:**
| action_type | points_value |
|---|---|
| VACCINATION | 100 |
| PREVENTIVE_EXAM | 150 |

---

## Contexto: points-service

```mermaid
erDiagram
    POINTS_ACCOUNT {
        uuid id PK
        uuid citizen_id UK "ref externo"
        int balance
        int total_earned
        int total_redeemed
        timestamp created_at
        timestamp updated_at
    }

    POINT_TRANSACTION {
        uuid id PK
        uuid account_id FK
        string transaction_type "CREDIT | DEBIT"
        int amount
        string reason
        uuid reference_id "healthActionId"
        string reference_type "HEALTH_ACTION"
        string idempotency_key UK "evita duplo processamento"
        timestamp created_at
    }

    POINTS_ACCOUNT ||--o{ POINT_TRANSACTION : "possui"
```

---

## Visão consolidada dos IDs externos

Os serviços se comunicam via **eventos Kafka**, carregando apenas IDs de referência — nunca fazendo JOIN entre bases de dados.

```mermaid
graph LR
    SUS["SUS_CITIZEN_RECORD\n(sus-mock-service, sem persistência)"]
    CIT["CITIZEN\n(citizen-service)"]
    HA["HEALTH_ACTION\n(health-action-service)"]
    PA["POINTS_ACCOUNT\n(points-service)"]
    PT["POINT_TRANSACTION\n(points-service)"]

    SUS -->|cpf via REST síncrono| CIT
    CIT -->|citizen_id via evento| HA
    CIT -->|citizen_id via evento| PA
    PA -->|account_id| PT
```

---

## Roadmap — Contextos das próximas fases

| Bounded Context | Serviço | Fase |
|---|---|---|
| Catálogo de recompensas (REWARD) | `reward-service` | Fase 1 |
| Resgates (REDEMPTION) | `redemption-service` | Fase 1 |
