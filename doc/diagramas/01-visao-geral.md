# Diagrama de Visão Geral — Arquitetura C4 (Container)

```mermaid
graph TB
    Cidadao(["👤 Cidadão\n(Usuário SUS)"])
    Admin(["👤 Admin SUS\n(Gestor)"])

    subgraph infra["Infraestrutura de Suporte"]
        DS["🔍 Discovery Server\nEureka"]
    end

    subgraph entrada["Camada de Entrada"]
        GW["🌐 API Gateway\nSpring Cloud Gateway\n:8080"]
    end

    subgraph dominio["Microsserviços de Domínio"]
        CIT["👥 citizen-service\n:8081"]
        HA["🏥 health-action-service\n:8082"]
        PT["⭐ points-service\n:8083"]
        SUS["🏛️ sus-mock-service\n:8087"]
    end

    subgraph mensageria["Barramento de Eventos"]
        KAFKA[["📨 Apache Kafka\nTópicos de domínio"]]
    end

    subgraph dados["Bancos de Dados (um por serviço)"]
        DB1[("citizen_db\nPostgreSQL")]
        DB2[("health_action_db\nPostgreSQL")]
        DB3[("points_db\nPostgreSQL")]
    end

    Cidadao -->|HTTPS REST| GW
    Admin -->|HTTPS REST| GW

    GW -->|HTTP| CIT
    GW -->|HTTP| HA
    GW -->|HTTP| PT
    GW -->|HTTP| SUS

    CIT -->|"REST síncrono (Feign)\nGET /sus/citizens"| SUS

    HA -->|"Publica\nhealth-action.registered"| KAFKA
    KAFKA -->|"Consome\nhealth-action.registered"| PT
    PT -->|"Publica\npoints.credited"| KAFKA

    CIT --- DB1
    HA --- DB2
    PT --- DB3

    GW -.->|"Registro"| DS
    CIT -.->|"Registro"| DS
    HA -.->|"Registro"| DS
    PT -.->|"Registro"| DS
    SUS -.->|"Registro"| DS
```

## Legenda

| Componente | Tecnologia | Porta |
|---|---|---|
| API Gateway | Spring Cloud Gateway | 8080 |
| Discovery Server | Netflix Eureka | 8761 |
| citizen-service | Spring Boot | 8081 |
| health-action-service | Spring Boot | 8082 |
| points-service | Spring Boot | 8083 |
| sus-mock-service | Spring Boot | 8087 |
| Apache Kafka | Kafka | 9092 |
| PostgreSQL (x3) | PostgreSQL 16 | 5432–5434 |

## Princípios

- **Comunicação síncrona (HTTP):** queries/commands do cliente externo via API Gateway, e a sincronização `citizen-service → sus-mock-service` (Feign), que simula a integração com um sistema externo (não é coordenação entre serviços de domínio)
- **Comunicação assíncrona (Kafka):** toda propagação de eventos de domínio entre serviços
- **Nenhum serviço de domínio chama outro diretamente** — toda coordenação entre eles ocorre via eventos
