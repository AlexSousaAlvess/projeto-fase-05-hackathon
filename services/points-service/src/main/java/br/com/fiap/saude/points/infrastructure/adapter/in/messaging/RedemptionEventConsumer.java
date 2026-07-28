package br.com.fiap.saude.points.infrastructure.adapter.in.messaging;

import br.com.fiap.saude.points.domain.port.in.DebitPointsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedemptionEventConsumer {

    private final DebitPointsUseCase debitPointsUseCase;

    @KafkaListener(topics = "redemption.requested", groupId = "points-service")
    public void onRedemptionRequested(RedemptionRequestedMessage message) {
        log.info("Recebido evento redemption.requested: redemptionId={}", message.redemptionId());
        debitPointsUseCase.debit(new DebitPointsUseCase.Command(
                message.citizenId(),
                message.pointsSpent(),
                message.redemptionId(),
                message.eventId().toString()
        ));
    }

    public record RedemptionRequestedMessage(
            UUID eventId, UUID redemptionId, UUID citizenId, UUID rewardId, String rewardName, int pointsSpent
    ) {}
}
