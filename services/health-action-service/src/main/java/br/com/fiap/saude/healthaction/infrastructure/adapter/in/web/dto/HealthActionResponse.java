package br.com.fiap.saude.healthaction.infrastructure.adapter.in.web.dto;

import br.com.fiap.saude.healthaction.domain.model.ActionStatus;
import br.com.fiap.saude.healthaction.domain.model.ActionType;
import br.com.fiap.saude.healthaction.domain.model.HealthAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record HealthActionResponse(
        UUID id,
        UUID citizenId,
        ActionType actionType,
        String description,
        ActionStatus status,
        int pointsValue,
        LocalDateTime registeredAt
) {
    public static HealthActionResponse from(HealthAction action) {
        return new HealthActionResponse(
                action.getId(), action.getCitizenId(), action.getActionType(),
                action.getDescription(), action.getStatus(), action.getPointsValue(),
                action.getRegisteredAt()
        );
    }
}
