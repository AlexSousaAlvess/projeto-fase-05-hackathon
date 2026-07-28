# Diagrama de Sequência — Registro de Ação de Saúde

```mermaid
sequenceDiagram
    actor Cidadao as Cidadão
    participant GW as API Gateway
    participant HA as health-action-service
    participant HA_DB as health_action_db
    participant KAFKA as Apache Kafka
    participant PT as points-service
    participant PT_DB as points_db

    Cidadao->>GW: POST /health-actions<br/>{citizenId, actionType, description, proofDocumentUrl}
    GW->>HA: POST /health-actions (roteado)

    HA->>HA: Valida tipo de ação e campos obrigatórios
    HA->>HA: Calcula pointsValue baseado no actionType

    HA->>HA_DB: INSERT health_action<br/>{citizenId, actionType, pointsValue, status: VALIDATED}
    HA_DB-->>HA: HealthAction salvo

    HA->>KAFKA: Publica health-action.registered<br/>{eventId, citizenId, actionId, actionType, pointsValue}

    HA-->>GW: 201 Created<br/>{actionId, actionType, pointsValue, status: VALIDATED}
    GW-->>Cidadao: 201 Created

    Note over KAFKA, PT: Processamento assíncrono

    KAFKA->>PT: Consome health-action.registered
    PT->>PT: Verifica idempotência (eventId)
    PT->>PT_DB: SELECT points_account WHERE citizen_id = ?

    alt Conta não existe
        PT->>PT_DB: INSERT points_account<br/>{citizenId, balance: 0}
    end

    PT->>PT_DB: UPDATE points_account SET balance += pointsValue
    PT->>PT_DB: INSERT point_transaction<br/>{type: CREDIT, amount, referenceId: actionId, idempotencyKey: eventId}
    PT_DB-->>PT: Transação persistida

    PT->>KAFKA: Publica points.credited<br/>{eventId, citizenId, amount, newBalance}
```

---

## Cenário Alternativo: Tipo de Ação Inválido

```mermaid
sequenceDiagram
    actor Cidadao as Cidadão
    participant GW as API Gateway
    participant HA as health-action-service

    Cidadao->>GW: POST /health-actions<br/>{actionType: "INVALIDO", ...}
    GW->>HA: POST /health-actions

    HA->>HA: Valida actionType
    HA-->>GW: 400 Bad Request<br/>{error: "Tipo de ação inválido",<br/>validValues: ["VACCINATION", "PREVENTIVE_EXAM"]}
    GW-->>Cidadao: 400 Bad Request
```

---

## Notas de Implementação

- O `eventId` é um UUID gerado pelo `health-action-service` e embarcado no evento Kafka
- O `points-service` armazena o `eventId` como `idempotency_key` na tabela `point_transaction` com constraint `UNIQUE` — garantindo que reentregas do Kafka não causem duplo crédito
- A resposta ao cidadão é **síncrona** (201 Created) e não aguarda o processamento assíncrono de pontos
- Notificação ao cidadão via `notification-service` é roadmap (Fase 1)
