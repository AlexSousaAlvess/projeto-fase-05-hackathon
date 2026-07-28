package br.com.fiap.saude.points.infrastructure.adapter.in.messaging;

import br.com.fiap.saude.points.domain.port.in.CreditPointsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthActionEventConsumer {

    private final CreditPointsUseCase creditPointsUseCase;

    @KafkaListener(topics = "health-action.registered", groupId = "points-service")
    public void onHealthActionRegistered(HealthActionRegisteredMessage message) {
        log.info("Recebido evento health-action.registered: actionId={}", message.actionId());
        creditPointsUseCase.credit(new CreditPointsUseCase.Command(
                message.citizenId(),
                message.pointsValue(),
                "Ação de saúde: " + message.actionType(),
                message.actionId(),
                message.eventId().toString()
        ));
    }

    public record HealthActionRegisteredMessage(
            UUID eventId, UUID actionId, UUID citizenId, String actionType, int pointsValue
    ) {}
}
