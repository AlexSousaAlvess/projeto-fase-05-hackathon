# Diagrama de Sequência — Resgate de Recompensa (Saga por Coreografia)

## Fluxo Feliz (Happy Path)

```mermaid
sequenceDiagram
    actor Cidadao as Cidadão
    participant GW as API Gateway
    participant RD as redemption-service
    participant RD_DB as redemption_db
    participant KAFKA as Apache Kafka
    participant PT as points-service
    participant PT_DB as points_db
    participant RW as reward-service
    participant RW_DB as reward_db
    participant NT as notification-service

    Cidadao->>GW: POST /resgates<br/>{rewardId}
    GW->>RD: POST /resgates (roteado)

    RD->>RD: Valida rewardId e campos
    RD->>RD_DB: INSERT redemption<br/>{citizenId, rewardId, pointsSpent, status: REQUESTED}
    RD_DB-->>RD: Redemption criado

    RD->>KAFKA: Publica redemption.requested<br/>{eventId, redemptionId, citizenId, rewardId, rewardName, pointsSpent}
    
    RD-->>GW: 202 Accepted<br/>{redemptionId, status: PROCESSING}
    GW-->>Cidadao: 202 Accepted

    Note over KAFKA, PT: Saga — Passo 1: Debitar pontos

    KAFKA->>PT: Consome redemption.requested
    PT->>PT: Verifica idempotência (eventId)
    PT->>PT_DB: SELECT balance WHERE citizen_id = ?
    PT_DB-->>PT: balance = 350

    PT->>PT: 350 >= 200? ✅ Saldo suficiente

    PT->>PT_DB: UPDATE balance -= pointsSpent
    PT->>PT_DB: INSERT point_transaction<br/>{type: DEBIT, amount: 200, referenceId: redemptionId}
    PT_DB-->>PT: OK

    PT->>KAFKA: Publica points.debited<br/>{eventId, citizenId, amount: 200, newBalance: 150, redemptionId}

    Note over KAFKA, RD: Saga — Passo 2: Aprovar resgate

    KAFKA->>RD: Consome points.debited
    RD->>RD_DB: UPDATE redemption SET status = APPROVED WHERE id = redemptionId
    
    RD->>KAFKA: Publica redemption.approved<br/>{eventId, redemptionId, citizenId, rewardId}

    KAFKA->>NT: Consome points.debited
    NT->>Cidadao: Notificação "200 pontos debitados. Processando resgate..."

    Note over KAFKA, RW: Saga — Passo 3: Conceder recompensa

    KAFKA->>RW: Consome redemption.approved
    RW->>RW_DB: UPDATE reward SET stock_quantity -= 1
    RW->>KAFKA: Publica reward.granted<br/>{eventId, redemptionId, citizenId, rewardId, rewardName, grantedAt}

    KAFKA->>RD: Consome reward.granted
    RD->>RD_DB: UPDATE redemption SET status = DELIVERED, processed_at = now()

    KAFKA->>NT: Consome reward.granted
    NT->>Cidadao: Notificação "🎉 Recompensa concedida!<br/>Crédito R$10 transporte aplicado."
```

---

## Fluxo Alternativo: Saldo Insuficiente (Compensação da Saga)

```mermaid
sequenceDiagram
    actor Cidadao as Cidadão
    participant RD as redemption-service
    participant RD_DB as redemption_db
    participant KAFKA as Apache Kafka
    participant PT as points-service
    participant PT_DB as points_db
    participant NT as notification-service

    Note over RD, PT: Após redemption.requested publicado...

    KAFKA->>PT: Consome redemption.requested
    PT->>PT_DB: SELECT balance WHERE citizen_id = ?
    PT_DB-->>PT: balance = 100

    PT->>PT: 100 >= 200? ❌ Saldo insuficiente

    PT->>KAFKA: Publica points.debit-failed<br/>{eventId, citizenId, redemptionId,<br/>currentBalance: 100, requiredAmount: 200, reason: "INSUFFICIENT_BALANCE"}

    KAFKA->>RD: Consome points.debit-failed
    RD->>RD_DB: UPDATE redemption SET status = CANCELLED,<br/>cancel_reason = "INSUFFICIENT_BALANCE"
    RD->>KAFKA: Publica redemption.cancelled<br/>{eventId, redemptionId, citizenId, reason}

    KAFKA->>NT: Consome points.debit-failed
    NT->>Cidadao: Notificação "❌ Saldo insuficiente.<br/>Você tem 100 pontos, mas precisa de 200."
```

---

## Consulta de Status (Síncrona)

```mermaid
sequenceDiagram
    actor Cidadao as Cidadão
    participant GW as API Gateway
    participant RD as redemption-service
    participant RD_DB as redemption_db

    Cidadao->>GW: GET /resgates/{redemptionId}
    GW->>RD: GET /resgates/{redemptionId}
    RD->>RD_DB: SELECT * FROM redemption WHERE id = ?
    RD_DB-->>RD: Redemption {status: DELIVERED, processedAt: ...}
    RD-->>GW: 200 OK {redemptionId, status, rewardName, processedAt}
    GW-->>Cidadao: 200 OK
```
