package br.com.fiap.saude.healthaction.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.healthaction.domain.model.ActionStatus;
import br.com.fiap.saude.healthaction.domain.model.ActionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_actions")
@Getter @Setter @NoArgsConstructor
public class HealthActionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID citizenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    private String description;
    private String proofDocumentUrl;

    @Enumerated(EnumType.STRING)
    private ActionStatus status;

    private int pointsValue;
    private LocalDateTime registeredAt;
}
