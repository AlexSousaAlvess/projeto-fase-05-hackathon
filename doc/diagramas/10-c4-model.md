# C4 Model — SaúdePoints

> O C4 Model descreve a arquitetura de software em quatro níveis de zoom progressivo:
> **Context → Container → Component → Code**. O nível 4 (Code) é omitido — o código-fonte é a documentação autoritativa nesse nível.

---

## Nível 1 — System Context (Contexto do Sistema)

Mostra o sistema como uma caixa preta e suas relações com atores e sistemas externos.

```mermaid
C4Context
  title Diagrama de Contexto — SaúdePoints

  Person(cidadao, "Cidadão SUS", "Usuário do SUS que registra ações de saúde e acumula pontos")
  Person(admin, "Admin SUS", "Gestor que dispara sincronizações e consulta dados")

  System(saudepoints, "SaúdePoints", "Plataforma de gamificação da saúde preventiva: registra ações de saúde, acumula pontos e futuramente permite resgatar recompensas em serviços públicos")

  System_Ext(cadsus, "CADSUS / RNDS", "Base nacional de cadastro de cidadãos do SUS (Rede Nacional de Dados em Saúde). No MVP é simulada pelo sus-mock-service.")

  System_Ext(beneficios, "Parceiros de Benefícios", "SPTrans, distribuidoras de energia, teatros — concedem os benefícios resgatados (roadmap Fase 1)")

  Rel(cidadao, saudepoints, "Registra ações de saúde e consulta saldo de pontos", "HTTPS REST")
  Rel(admin, saudepoints, "Sincroniza base de cidadãos e monitora o sistema", "HTTPS REST")
  Rel(saudepoints, cadsus, "Consome cadastro de cidadãos para ingesta inicial", "REST / Feign (mock no MVP)")
  Rel(saudepoints, beneficios, "Notifica concessão de benefícios resgatados", "Integração futura — Fase 1")
```

---

## Nível 2 — Container (Contêineres)

Mostra os processos executáveis, bancos de dados e o barramento de mensagens que compõem o sistema.

```mermaid
C4Container
  title Diagrama de Contêineres — SaúdePoints

  Person(cidadao, "Cidadão SUS", "")
  Person(admin, "Admin SUS", "")

  System_Ext(cadsus, "sus-mock-service / CADSUS", "Dataset fixo de cidadãos simulando o CADSUS")

  System_Boundary(saudepoints, "SaúdePoints") {

    Container(gateway, "api-gateway", "Spring Cloud Gateway · :8080", "Ponto de entrada único: roteamento, load balancing via Eureka, circuit breaker Resilience4j")

    Container(eureka, "discovery-server", "Netflix Eureka · :8761", "Registro e descoberta de serviços — todos os serviços se registram aqui ao iniciar")

    Container(citizen, "citizen-service", "Spring Boot · :8081 · PostgreSQL", "Gerencia perfis de cidadãos; sincroniza cadastros a partir do sus-mock-service via Feign. Não permite autocadastro.")

    Container(healthaction, "health-action-service", "Spring Boot · :8082 · PostgreSQL", "Recebe e valida ações de saúde (VACCINATION, PREVENTIVE_EXAM); calcula pontos e publica evento no Kafka")

    Container(points, "points-service", "Spring Boot · :8083 · PostgreSQL", "Mantém saldo e histórico de transações de pontos; consome eventos do Kafka com idempotência garantida")

    Container(susmock, "sus-mock-service", "Spring Boot · :8087 · sem banco", "Simula a base nacional de cidadãos (CADSUS) com dataset fixo em memória. Test double do sistema externo real.")

    ContainerDb(citizendb, "citizen_db", "PostgreSQL 16", "Armazena perfis de cidadãos sincronizados")
    ContainerDb(hadb, "health_action_db", "PostgreSQL 16", "Armazena ações de saúde registradas e validadas")
    ContainerDb(pointsdb, "points_db", "PostgreSQL 16", "Armazena contas de pontos e histórico de transações")

    ContainerQueue(kafka, "Apache Kafka", "Kafka · :9092", "Barramento de eventos assíncronos entre health-action-service e points-service")
  }

  Rel(cidadao, gateway, "Registra ações, consulta saldo", "HTTPS REST")
  Rel(admin, gateway, "Sincroniza cidadãos, monitora", "HTTPS REST")

  Rel(gateway, citizen, "Roteia /citizens/**", "HTTP lb://citizen-service")
  Rel(gateway, healthaction, "Roteia /health-actions/**", "HTTP lb://health-action-service")
  Rel(gateway, points, "Roteia /points/**", "HTTP lb://points-service")
  Rel(gateway, susmock, "Roteia /sus/**", "HTTP lb://sus-mock-service")

  Rel(gateway, eureka, "Descobre instâncias", "HTTP")
  Rel(citizen, eureka, "Registra-se", "HTTP")
  Rel(healthaction, eureka, "Registra-se", "HTTP")
  Rel(points, eureka, "Registra-se", "HTTP")
  Rel(susmock, eureka, "Registra-se", "HTTP")

  Rel(citizen, susmock, "Busca lista de cidadãos (POST /sync-sus)", "REST síncrono via Feign")

  Rel(citizen, citizendb, "Lê / Grava", "JPA / PostgreSQL")
  Rel(healthaction, hadb, "Lê / Grava", "JPA / PostgreSQL")
  Rel(points, pointsdb, "Lê / Grava", "JPA / PostgreSQL")

  Rel(healthaction, kafka, "Publica health-action.registered", "Kafka Producer")
  Rel(kafka, points, "Entrega health-action.registered", "Kafka Consumer")
  Rel(points, kafka, "Publica points.credited", "Kafka Producer")

  Rel(citizen, cadsus, "Simula integração com CADSUS real", "Futuro: RNDS")
```

---

## Nível 3 — Component: citizen-service

Mostra os componentes internos seguindo a Arquitetura Hexagonal (Ports & Adapters).

```mermaid
C4Component
  title Componentes — citizen-service

  Person(admin, "Admin SUS", "")
  System_Ext(susmock, "sus-mock-service", "Expõe GET /sus/citizens")
  ContainerDb(citizendb, "citizen_db", "PostgreSQL")

  Container_Boundary(citizen, "citizen-service") {

    Component(controller, "CitizenController", "REST Controller (adapter/in/web)", "Expõe POST /citizens/sync-sus e GET /citizens · GET /citizens/{id}")

    Component(syncUC, "SyncCitizensFromSusUseCase", "Port de entrada (domain/port/in)", "Contrato para disparar sincronização com o SUS mock")
    Component(findUC, "FindCitizenUseCase", "Port de entrada (domain/port/in)", "Contrato para consulta de cidadão por ID ou listagem")

    Component(service, "CitizenService", "Application Service (application/service)", "Implementa SyncCitizensFromSusUseCase e FindCitizenUseCase. Orquestra chamada ao SUS e persistência. Ignora CPFs já existentes (idempotência).")

    Component(citizen_model, "Citizen", "Domain Model (domain/model)", "Entidade de domínio: id, cpf, name, email, phone, birthDate, status, timestamps")

    Component(susPort, "SusClientPort", "Port de saída (domain/port/out)", "Contrato para buscar cidadãos do sistema externo SUS")
    Component(repoPort, "CitizenRepositoryPort", "Port de saída (domain/port/out)", "Contrato para persistência e consulta de cidadãos")

    Component(susAdapter, "SusClientAdapter", "Adapter de saída (adapter/out/client)", "Implementa SusClientPort via SusFeignClient; mapeia DTO → SusCitizenData do domínio")
    Component(feignClient, "SusFeignClient", "Feign Client (adapter/out/client)", "@FeignClient(name='sus-mock-service') — chama GET /sus/citizens via Eureka")

    Component(persistence, "CitizenPersistenceAdapter", "Adapter de saída (adapter/out/persistence)", "Implementa CitizenRepositoryPort via Spring Data JPA")
  }

  Rel(admin, controller, "POST /citizens/sync-sus · GET /citizens", "HTTP REST")
  Rel(controller, syncUC, "syncAll()", "Interface")
  Rel(controller, findUC, "findById() · findAll()", "Interface")
  Rel(service, syncUC, "implements", "")
  Rel(service, findUC, "implements", "")
  Rel(service, citizen_model, "cria / usa", "")
  Rel(service, susPort, "fetchCitizens()", "Interface")
  Rel(service, repoPort, "save() · existsByCpf() · findById() · findAll()", "Interface")
  Rel(susAdapter, susPort, "implements", "")
  Rel(susAdapter, feignClient, "delega chamada HTTP", "")
  Rel(feignClient, susmock, "GET /sus/citizens", "HTTP via Eureka lb://")
  Rel(persistence, repoPort, "implements", "")
  Rel(persistence, citizendb, "INSERT / SELECT", "JPA")
```

---

## Nível 3 — Component: health-action-service

```mermaid
C4Component
  title Componentes — health-action-service

  Person(cidadao, "Cidadão SUS", "")
  ContainerDb(hadb, "health_action_db", "PostgreSQL")
  ContainerQueue(kafka, "Apache Kafka", "Tópico: health-action.registered")

  Container_Boundary(healthaction, "health-action-service") {

    Component(hacontroller, "HealthActionController", "REST Controller (adapter/in/web)", "Expõe POST /health-actions e GET /health-actions/{id} · GET /health-actions/citizen/{citizenId}")

    Component(registerUC, "RegisterHealthActionUseCase", "Port de entrada (domain/port/in)", "Contrato para registrar uma nova ação de saúde validada")
    Component(findHaUC, "FindHealthActionUseCase", "Port de entrada (domain/port/in)", "Contrato para consulta de ações por ID ou por cidadão")

    Component(haservice, "HealthActionService", "Application Service (application/service)", "Valida actionType, calcula pointsValue (VACCINATION=100, PREVENTIVE_EXAM=150), persiste com status VALIDATED e publica evento Kafka")

    Component(ha_model, "HealthAction", "Domain Model (domain/model)", "Entidade: id, citizenId, actionType, description, proofDocumentUrl, status, pointsValue, registeredAt")

    Component(haRepoPort, "HealthActionRepositoryPort", "Port de saída (domain/port/out)", "Contrato de persistência de ações de saúde")
    Component(haEventPort, "HealthActionEventPublisherPort", "Port de saída (domain/port/out)", "Contrato para publicar eventos de domínio no Kafka")

    Component(haPersistence, "HealthActionPersistenceAdapter", "Adapter de saída (adapter/out/persistence)", "Implementa HealthActionRepositoryPort via Spring Data JPA")
    Component(kafkaPublisher, "KafkaHealthActionEventPublisher", "Adapter de saída (adapter/out/messaging)", "Implementa HealthActionEventPublisherPort; publica HealthActionRegisteredEvent no tópico health-action.registered via KafkaTemplate")
  }

  Rel(cidadao, hacontroller, "POST /health-actions", "HTTP REST via Gateway")
  Rel(hacontroller, registerUC, "register(command)", "Interface")
  Rel(hacontroller, findHaUC, "findById() · findByCitizenId()", "Interface")
  Rel(haservice, registerUC, "implements", "")
  Rel(haservice, findHaUC, "implements", "")
  Rel(haservice, ha_model, "cria / usa", "")
  Rel(haservice, haRepoPort, "save() · findById() · findByCitizenId()", "Interface")
  Rel(haservice, haEventPort, "publish(event)", "Interface")
  Rel(haPersistence, haRepoPort, "implements", "")
  Rel(haPersistence, hadb, "INSERT / SELECT", "JPA")
  Rel(kafkaPublisher, haEventPort, "implements", "")
  Rel(kafkaPublisher, kafka, "KafkaTemplate.send('health-action.registered', event)", "Kafka Producer")
```

---

## Nível 3 — Component: points-service

```mermaid
C4Component
  title Componentes — points-service

  Person(cidadao, "Cidadão SUS", "")
  ContainerDb(pointsdb, "points_db", "PostgreSQL")
  ContainerQueue(kafkaIn, "Kafka: health-action.registered", "Evento de entrada")
  ContainerQueue(kafkaOut, "Kafka: points.credited", "Evento de saída")

  Container_Boundary(points, "points-service") {

    Component(ptcontroller, "PointsController", "REST Controller (adapter/in/web)", "Expõe GET /points/{citizenId} — retorna saldo e histórico de transações")

    Component(haConsumer, "HealthActionEventConsumer", "Kafka Consumer (adapter/in/messaging)", "Consome health-action.registered; chama CreditPointsUseCase. Grupo: points-service-group")

    Component(creditUC, "CreditPointsUseCase", "Port de entrada (domain/port/in)", "Contrato para creditar pontos na conta do cidadão")
    Component(debitUC, "DebitPointsUseCase", "Port de entrada (domain/port/in)", "Contrato para debitar pontos (roadmap — resgate)")
    Component(balanceUC, "GetBalanceUseCase", "Port de entrada (domain/port/in)", "Contrato para consultar saldo e histórico")

    Component(ptservice, "PointsService", "Application Service (application/service)", "Implementa todos os use cases. Verifica idempotência pelo eventId antes de creditar. Cria conta automaticamente se não existir.")

    Component(account_model, "PointsAccount", "Domain Model (domain/model)", "Entidade: id, citizenId (UK), balance, totalEarned, totalRedeemed, timestamps")
    Component(tx_model, "PointTransaction", "Domain Model (domain/model)", "Entidade: id, accountId, type (CREDIT/DEBIT), amount, reason, referenceId, idempotencyKey (UK), createdAt")

    Component(accountRepo, "PointsAccountRepositoryPort", "Port de saída (domain/port/out)", "Contrato de persistência de contas de pontos")
    Component(txRepo, "PointTransactionRepositoryPort", "Port de saída (domain/port/out)", "Contrato de persistência de transações")
    Component(eventPort, "PointsEventPublisherPort", "Port de saída (domain/port/out)", "Contrato para publicar events no Kafka")

    Component(ptPersistence, "PointsPersistenceAdapter", "Adapter de saída (adapter/out/persistence)", "Implementa PointsAccountRepositoryPort e PointTransactionRepositoryPort via Spring Data JPA")
    Component(ptKafka, "KafkaPointsEventPublisher", "Adapter de saída (adapter/out/messaging)", "Implementa PointsEventPublisherPort; publica PointsCreditedEvent no tópico points.credited via KafkaTemplate")
  }

  Rel(cidadao, ptcontroller, "GET /points/{citizenId}", "HTTP REST via Gateway")
  Rel(ptcontroller, balanceUC, "getBalance(citizenId)", "Interface")

  Rel(kafkaIn, haConsumer, "Entrega health-action.registered", "Kafka Consumer")
  Rel(haConsumer, creditUC, "credit(command)", "Interface")

  Rel(ptservice, creditUC, "implements", "")
  Rel(ptservice, debitUC, "implements", "")
  Rel(ptservice, balanceUC, "implements", "")
  Rel(ptservice, account_model, "cria / atualiza", "")
  Rel(ptservice, tx_model, "cria", "")
  Rel(ptservice, accountRepo, "findByCitizenId() · save()", "Interface")
  Rel(ptservice, txRepo, "existsByIdempotencyKey() · save()", "Interface")
  Rel(ptservice, eventPort, "publish(event)", "Interface")

  Rel(ptPersistence, accountRepo, "implements", "")
  Rel(ptPersistence, txRepo, "implements", "")
  Rel(ptPersistence, pointsdb, "INSERT / SELECT / UPDATE", "JPA")

  Rel(ptKafka, eventPort, "implements", "")
  Rel(ptKafka, kafkaOut, "KafkaTemplate.send('points.credited', event)", "Kafka Producer")
```

---

## Resumo das dependências entre níveis

| Nível | Diagrama | Arquivo |
|---|---|---|
| 1 — Context | Atores + sistemas externos | Este arquivo (`10-c4-model.md`) |
| 2 — Container | Serviços, DBs, Kafka | Este arquivo (`10-c4-model.md`) |
| 3 — Component (citizen) | Hexagonal: controller → service → adapters | Este arquivo (`10-c4-model.md`) |
| 3 — Component (health-action) | Hexagonal: controller → service → Kafka publisher | Este arquivo (`10-c4-model.md`) |
| 3 — Component (points) | Hexagonal: consumer + controller → service → adapters | Este arquivo (`10-c4-model.md`) |
| 4 — Code | Implementação Java | Código-fonte em `services/` |
