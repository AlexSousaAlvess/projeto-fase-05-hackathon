package br.com.fiap.saude.healthaction.infrastructure.adapter.out.messaging;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;
import br.com.fiap.saude.healthaction.domain.port.out.HealthActionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthActionEventPublisher implements HealthActionEventPublisherPort {

    static final String TOPIC = "health-action.registered";

    private final KafkaTemplate<String, HealthActionRegisteredEvent> kafkaTemplate;

    @Override
    public void publishHealthActionRegistered(HealthAction action) {
        HealthActionRegisteredEvent event = new HealthActionRegisteredEvent(
                UUID.randomUUID(),
                action.getId(),
                action.getCitizenId(),
                action.getActionType().name(),
                action.getPointsValue(),
                action.getRegisteredAt()
        );
        kafkaTemplate.send(TOPIC, action.getCitizenId().toString(), event);
        log.info("Evento publicado: {} para cidadão {}", TOPIC, action.getCitizenId());
    }
}
