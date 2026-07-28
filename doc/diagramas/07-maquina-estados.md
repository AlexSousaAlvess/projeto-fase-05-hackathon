# Diagrama de Máquina de Estados

## HealthAction — Status da Ação de Saúde

```mermaid
stateDiagram-v2
    [*] --> PENDING : Cidadão registra ação\nPOST /acoes-saude

    PENDING --> VALIDATED : Admin valida\n(fase futura)\nOU auto-validado no MVP
    PENDING --> REJECTED : Admin rejeita\n(fase futura)

    VALIDATED --> [*] : Evento health-action.registered\npublicado → pontos creditados
    REJECTED --> [*] : Evento health-action.rejected\npublicado → cidadão notificado

    note right of PENDING
        No MVP: transição automática
        para VALIDATED no momento
        do registro
    end note
```

---

## Redemption — Status do Resgate

```mermaid
stateDiagram-v2
    [*] --> REQUESTED : Cidadão solicita resgate\nPOST /resgates

    REQUESTED --> PROCESSING : Evento redemption.requested\npublicado ao Kafka

    PROCESSING --> APPROVED : points-service confirma débito\n(points.debited recebido)
    PROCESSING --> CANCELLED : Saldo insuficiente\n(points.debit-failed recebido)

    APPROVED --> DELIVERED : reward-service concede recompensa\n(reward.granted recebido)
    APPROVED --> CANCELLED : Recompensa sem estoque\n(fase futura)

    DELIVERED --> [*] : Fluxo completo\ncidadão notificado ✅
    CANCELLED --> [*] : Fluxo cancelado\ncidadão notificado ❌

    note right of PROCESSING
        Estado intermediário assíncrono.
        Cidadão pode consultar
        GET /resgates/{id}
    end note
```

---

## PointsAccount — Transições de Saldo

```mermaid
stateDiagram-v2
    [*] --> ZERO : Conta criada\nautomaticamente no primeiro crédito

    ZERO --> POSITIVO : CREDIT — Ação de saúde validada

    POSITIVO --> POSITIVO : CREDIT — nova ação de saúde\nOU DEBIT — resgate (saldo > 0 após)
    POSITIVO --> ZERO : DEBIT — resgate consome todo o saldo

    note right of ZERO
        Saldo nunca fica negativo.
        Débito é recusado se
        balance < pointsSpent
    end note
```

---

## Resumo das Transições por Evento

| Entidade | Estado Atual | Evento/Trigger | Próximo Estado |
|---|---|---|---|
| HealthAction | — | POST /acoes-saude | VALIDATED (MVP) |
| Redemption | — | POST /resgates | REQUESTED |
| Redemption | REQUESTED | redemption.requested publicado | PROCESSING |
| Redemption | PROCESSING | points.debited recebido | APPROVED |
| Redemption | PROCESSING | points.debit-failed recebido | CANCELLED |
| Redemption | APPROVED | reward.granted recebido | DELIVERED |
