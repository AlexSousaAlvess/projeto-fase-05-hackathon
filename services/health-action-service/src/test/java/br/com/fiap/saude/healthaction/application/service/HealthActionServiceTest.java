package br.com.fiap.saude.healthaction.application.service;

import br.com.fiap.saude.healthaction.domain.model.ActionStatus;
import br.com.fiap.saude.healthaction.domain.model.ActionType;
import br.com.fiap.saude.healthaction.domain.model.HealthAction;
import br.com.fiap.saude.healthaction.domain.port.in.RegisterHealthActionUseCase;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionEventPublisherPort;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthActionServiceTest {

    @Mock
    private HealthActionRepositoryPort repository;

    @Mock
    private HealthActionEventPublisherPort eventPublisher;

    @InjectMocks
    private HealthActionService service;

    @Test
    void register_savesActionAndPublishesEvent() {
        UUID citizenId = UUID.randomUUID();
        RegisterHealthActionUseCase.Command command = new RegisterHealthActionUseCase.Command(
                citizenId, ActionType.VACCINATION, "Vacina da gripe", "http://proof.url"
        );
        HealthAction saved = buildAction(citizenId, ActionType.VACCINATION);
        when(repository.save(any())).thenReturn(saved);

        HealthAction result = service.register(command);

        assertThat(result).isNotNull();
        verify(repository).save(any(HealthAction.class));
        verify(eventPublisher).publishHealthActionRegistered(saved);
    }

    @ParameterizedTest
    @EnumSource(ActionType.class)
    void register_assignsCorrectPointsValuePerActionType(ActionType actionType) {
        UUID citizenId = UUID.randomUUID();
        RegisterHealthActionUseCase.Command command = new RegisterHealthActionUseCase.Command(
                citizenId, actionType, "desc", null
        );
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.register(command);

        ArgumentCaptor<HealthAction> captor = ArgumentCaptor.forClass(HealthAction.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPointsValue()).isEqualTo(actionType.getPointsValue());
        assertThat(captor.getValue().getStatus()).isEqualTo(ActionStatus.VALIDATED);
    }

    @Test
    void findByCitizenId_returnsListFromRepository() {
        UUID citizenId = UUID.randomUUID();
        List<HealthAction> actions = List.of(buildAction(citizenId, ActionType.PREVENTIVE_EXAM));
        when(repository.findByCitizenId(citizenId)).thenReturn(actions);

        List<HealthAction> result = service.findByCitizenId(citizenId);

        assertThat(result).hasSize(1);
        verify(repository).findByCitizenId(citizenId);
    }

    @Test
    void findByCitizenId_returnsEmptyListWhenNoActions() {
        UUID citizenId = UUID.randomUUID();
        when(repository.findByCitizenId(citizenId)).thenReturn(List.of());

        List<HealthAction> result = service.findByCitizenId(citizenId);

        assertThat(result).isEmpty();
    }

    private HealthAction buildAction(UUID citizenId, ActionType type) {
        return HealthAction.builder()
                .id(UUID.randomUUID())
                .citizenId(citizenId)
                .actionType(type)
                .status(ActionStatus.VALIDATED)
                .pointsValue(type.getPointsValue())
                .build();
    }
}
