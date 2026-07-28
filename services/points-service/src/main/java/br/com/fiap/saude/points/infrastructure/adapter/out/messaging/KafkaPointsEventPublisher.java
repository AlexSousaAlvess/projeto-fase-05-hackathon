package br.com.fiap.saude.points.infrastructure.adapter.out.messaging;

import br.com.fiap.saude.points.domain.model.PointsAccount;
import br.com.fiap.saude.points.domain.port.out.PointsEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPointsEventPublisher implements PointsEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPointsCredited(PointsAccount account, int amount, String reason) {
        var event = new PointsCreditedEvent(UUID.randomUUID(), account.getCitizenId(), amount, account.getBalance(), reason, LocalDateTime.now());
        kafkaTemplate.send("points.credited", account.getCitizenId().toString(), event);
        log.info("Evento publicado: points.credited para cidadão {}", account.getCitizenId());
    }

    @Override
    public void publishPointsDebited(PointsAccount account, int amount, UUID redemptionId) {
        var event = new PointsDebitedEvent(UUID.randomUUID(), account.getCitizenId(), amount, account.getBalance(), redemptionId, LocalDateTime.now());
        kafkaTemplate.send("points.debited", account.getCitizenId().toString(), event);
        log.info("Evento publicado: points.debited para cidadão {}", account.getCitizenId());
    }

    @Override
    public void publishPointsDebitFailed(UUID citizenId, UUID redemptionId, int currentBalance, int required) {
        var event = new PointsDebitFailedEvent(UUID.randomUUID(), citizenId, redemptionId, currentBalance, required, "INSUFFICIENT_BALANCE");
        kafkaTemplate.send("points.debit-failed", citizenId.toString(), event);
        log.info("Evento publicado: points.debit-failed para cidadão {}", citizenId);
    }

    public record PointsCreditedEvent(UUID eventId, UUID citizenId, int amount, int newBalance, String reason, LocalDateTime createdAt) {}
    public record PointsDebitedEvent(UUID eventId, UUID citizenId, int amount, int newBalance, UUID redemptionId, LocalDateTime createdAt) {}
    public record PointsDebitFailedEvent(UUID eventId, UUID citizenId, UUID redemptionId, int currentBalance, int requiredAmount, String reason) {}
}
