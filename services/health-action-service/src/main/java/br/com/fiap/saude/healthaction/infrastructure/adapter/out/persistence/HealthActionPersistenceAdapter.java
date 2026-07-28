package br.com.fiap.saude.healthaction.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HealthActionPersistenceAdapter implements HealthActionRepositoryPort {

    private final HealthActionJpaRepository jpaRepository;

    @Override
    public HealthAction save(HealthAction action) {
        HealthActionEntity entity = toEntity(action);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<HealthAction> findByCitizenId(UUID citizenId) {
        return jpaRepository.findByCitizenId(citizenId).stream().map(this::toDomain).toList();
    }

    private HealthActionEntity toEntity(HealthAction action) {
        HealthActionEntity entity = new HealthActionEntity();
        entity.setId(action.getId());
        entity.setCitizenId(action.getCitizenId());
        entity.setActionType(action.getActionType());
        entity.setDescription(action.getDescription());
        entity.setProofDocumentUrl(action.getProofDocumentUrl());
        entity.setStatus(action.getStatus());
        entity.setPointsValue(action.getPointsValue());
        entity.setRegisteredAt(action.getRegisteredAt());
        return entity;
    }

    private HealthAction toDomain(HealthActionEntity entity) {
        return HealthAction.builder()
                .id(entity.getId())
                .citizenId(entity.getCitizenId())
                .actionType(entity.getActionType())
                .description(entity.getDescription())
                .proofDocumentUrl(entity.getProofDocumentUrl())
                .status(entity.getStatus())
                .pointsValue(entity.getPointsValue())
                .registeredAt(entity.getRegisteredAt())
                .build();
    }
}
