package br.com.fiap.saude.healthaction.application.service;

import br.com.fiap.saude.healthaction.domain.model.ActionStatus;
import br.com.fiap.saude.healthaction.domain.model.HealthAction;
import br.com.fiap.saude.healthaction.domain.port.in.FindHealthActionUseCase;
import br.com.fiap.saude.healthaction.domain.port.in.RegisterHealthActionUseCase;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionEventPublisherPort;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthActionService implements RegisterHealthActionUseCase, FindHealthActionUseCase {

    private final HealthActionRepositoryPort repository;
    private final HealthActionEventPublisherPort eventPublisher;

    @Override
    public HealthAction register(Command command) {
        HealthAction action = HealthAction.builder()
                .id(UUID.randomUUID())
                .citizenId(command.citizenId())
                .actionType(command.actionType())
                .description(command.description())
                .proofDocumentUrl(command.proofDocumentUrl())
                .status(ActionStatus.VALIDATED)
                .pointsValue(command.actionType().getPointsValue())
                .registeredAt(LocalDateTime.now())
                .build();

        HealthAction saved = repository.save(action);
        eventPublisher.publishHealthActionRegistered(saved);
        return saved;
    }

    @Override
    public List<HealthAction> findByCitizenId(UUID citizenId) {
        return repository.findByCitizenId(citizenId);
    }
}
