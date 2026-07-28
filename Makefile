SHELL := /bin/bash

SERVICES := discovery-server api-gateway citizen-service \
            health-action-service points-service sus-mock-service

LOG_DIR := logs
PID_DIR := .pids

.DEFAULT_GOAL := help

.PHONY: help clean compile test build package \
        infra-up infra-down \
        start stop status logs \
        $(addprefix run-,$(SERVICES))

help:
	@echo "Build:"
	@echo "  make clean        - mvn clean"
	@echo "  make compile      - mvn compile"
	@echo "  make test         - mvn test"
	@echo "  make build        - clean + compile + test"
	@echo "  make package      - mvn clean package -DskipTests"
	@echo ""
	@echo "Infra (Postgres + Kafka via docker compose):"
	@echo "  make infra-up     - sobe bancos de dados e Kafka"
	@echo "  make infra-down   - derruba a infra"
	@echo ""
	@echo "Projeto completo:"
	@echo "  make start        - sobe infra + todos os microsserviços"
	@echo "  make stop         - para todos os microsserviços + infra (remove volumes)"
	@echo "  make status       - mostra quais serviços estão rodando"
	@echo "  make logs         - segue os logs de todos os serviços"
	@echo ""
	@echo "Serviço individual:"
	@echo "  make run-<nome>   - ex: make run-citizen-service"

## --- Build ---

clean:
	mvn clean

compile:
	mvn compile

test:
	mvn test

build: clean compile test

package:
	mvn clean package -DskipTests

## --- Infra (bancos + Kafka) ---

infra-up:
	docker compose up -d kafka citizen-db health-action-db points-db

infra-down:
	docker compose down

## --- Serviço individual ---

$(addprefix run-,$(SERVICES)):
	mvn spring-boot:run -pl services/$(@:run-%=%)

## --- Projeto completo ---

start: infra-up
	@mkdir -p $(LOG_DIR) $(PID_DIR)
	@for s in $(SERVICES); do \
		echo "Iniciando $$s..."; \
		nohup mvn spring-boot:run -pl services/$$s > $(LOG_DIR)/$$s.log 2>&1 & \
		echo $$! > $(PID_DIR)/$$s.pid; \
		sleep 5; \
	done
	@echo "Todos os serviços iniciados. Logs em $(LOG_DIR)/"

stop:
	@for s in $(SERVICES); do \
		if [ -f $(PID_DIR)/$$s.pid ]; then \
			pid=$$(cat $(PID_DIR)/$$s.pid); \
			echo "Parando $$s (pid $$pid)..."; \
			pkill -P $$pid 2>/dev/null || true; \
			kill $$pid 2>/dev/null || true; \
			rm -f $(PID_DIR)/$$s.pid; \
		fi; \
	done
	docker compose down -v

status:
	@for s in $(SERVICES); do \
		if [ -f $(PID_DIR)/$$s.pid ] && kill -0 $$(cat $(PID_DIR)/$$s.pid) 2>/dev/null; then \
			echo "$$s: RUNNING (pid $$(cat $(PID_DIR)/$$s.pid))"; \
		else \
			echo "$$s: STOPPED"; \
		fi; \
	done

logs:
	tail -f $(LOG_DIR)/*.log
