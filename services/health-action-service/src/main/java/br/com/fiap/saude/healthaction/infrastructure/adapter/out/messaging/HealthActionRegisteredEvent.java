package br.com.fiap.saude.healthaction.infrastructure.adapter.out.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record HealthActionRegisteredEvent(
        UUID eventId,
        UUID actionId,
        UUID citizenId,
        String actionType,
        int pointsValue,
        LocalDateTime registeredAt
) {}
