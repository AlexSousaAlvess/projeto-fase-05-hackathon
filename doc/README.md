# Documentação — Gamificação da Saúde Preventiva

## Postman

Collection para testar o MVP: [`postman/saude-gamificada.postman_collection.json`](../postman/saude-gamificada.postman_collection.json)

Importe no Postman e execute as pastas em ordem (SUS (mock) → Cidadãos → Ações de Saúde → Pontos → Recompensas). Os IDs gerados (cidadão, recompensa) são capturados automaticamente entre as requisições. A variável `baseUrl` aponta para o `api-gateway` (`http://localhost:8080`).

## Diagramas

| Diagrama | Arquivo |
|---|---|
| 01 — Visão Geral (C4 Container) | [diagramas/01-visao-geral.md](./diagramas/01-visao-geral.md) |
| 02 — Casos de Uso | [diagramas/02-casos-de-uso.md](./diagramas/02-casos-de-uso.md) |
| 03 — Modelo de Entidades (por bounded context) | [diagramas/03-modelo-entidades.md](./diagramas/03-modelo-entidades.md) |
| 04 — Fluxo de Eventos (EDD) | [diagramas/04-fluxo-eventos.md](./diagramas/04-fluxo-eventos.md) |
| 05 — Sequência: Registro de Ação de Saúde | [diagramas/05-sequencia-acao-saude.md](./diagramas/05-sequencia-acao-saude.md) |
| 06 — Sequência: Resgate de Recompensa (Saga) | [diagramas/06-sequencia-resgate.md](./diagramas/06-sequencia-resgate.md) |
| 07 — Máquina de Estados | [diagramas/07-maquina-estados.md](./diagramas/07-maquina-estados.md) |
| 08 — Implantação (Docker Compose) | [diagramas/08-implantacao.md](./diagramas/08-implantacao.md) |
| 09 — Comunicação entre Microsserviços | [diagramas/09-comunicacao-microsservicos.md](./diagramas/09-comunicacao-microsservicos.md) |
