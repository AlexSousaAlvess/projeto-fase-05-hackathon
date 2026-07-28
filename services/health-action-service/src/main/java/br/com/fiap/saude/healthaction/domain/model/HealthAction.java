package br.com.fiap.saude.healthaction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class HealthAction {
    private UUID id;
    private UUID citizenId;
    private ActionType actionType;
    private String description;
    private String proofDocumentUrl;
    private ActionStatus status;
    private int pointsValue;
    private LocalDateTime registeredAt;
}
