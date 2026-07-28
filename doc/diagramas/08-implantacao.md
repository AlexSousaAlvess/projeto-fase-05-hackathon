# Diagrama de Implantação — Ambiente Local (Docker Compose)

```mermaid
graph TB
    subgraph host["Máquina do Desenvolvedor"]
        subgraph docker["Docker Compose Network: saude-gamificada"]

            subgraph infra_containers["Infraestrutura"]
                KAFKA_C["kafka\nApache Kafka\n:9092"]
                DSC["discovery-server\n:8761"]
            end

            subgraph svc_containers["Serviços de Domínio"]
                GW_C["api-gateway\n:8080"]
                CIT_C["citizen-service\n:8081"]
                HA_C["health-action-service\n:8082"]
                PT_C["points-service\n:8083"]
                SUS_C["sus-mock-service\n:8087"]
            end

            subgraph db_containers["Bancos de Dados"]
                DB1_C[("citizen-db\nPostgreSQL\n:5432")]
                DB2_C[("health-action-db\nPostgreSQL\n:5433")]
                DB3_C[("points-db\nPostgreSQL\n:5434")]
            end
        end

        Postman["🧪 Postman / Swagger UI\n(testes manuais)"]
    end

    KAFKA_C --> HA_C
    KAFKA_C --> PT_C

    DSC -.->|"registry"| GW_C

    GW_C --> CIT_C
    GW_C --> HA_C
    GW_C --> PT_C
    GW_C --> SUS_C

    CIT_C -->|"Feign"| SUS_C

    CIT_C --- DB1_C
    HA_C --- DB2_C
    PT_C --- DB3_C

    Postman -->|":8080"| GW_C
```

---

## Ordem de Inicialização

```
1. kafka           (broker sem Zookeeper — modo KRaft)
2. *-db (x3)       (independentes, sobem em paralelo)
3. discovery-server
4. citizen-service, health-action-service, points-service, sus-mock-service
   (dependem de: discovery-server, kafka [exceto sus-mock-service], seus respectivos DBs)
5. api-gateway     (depende de: discovery-server + todos os serviços registrados)
```

---

## Comandos de Desenvolvimento Local

```bash
# Subir toda a stack
docker compose up -d

# Verificar saúde dos serviços
docker compose ps

# Acompanhar logs de um serviço específico
docker compose logs -f health-action-service

# Acessar Eureka Dashboard
open http://localhost:8761

# Parar tudo
docker compose down

# Parar e remover volumes (limpa bancos)
docker compose down -v
```

---

## Swagger UI por Serviço

| Serviço | URL direta |
|---|---|
| citizen-service | http://localhost:8081/swagger-ui.html |
| health-action-service | http://localhost:8082/swagger-ui.html |
| points-service | http://localhost:8083/swagger-ui.html |
| sus-mock-service | http://localhost:8087/swagger-ui.html |
