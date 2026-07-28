package br.com.fiap.saude.healthaction.domain.port.out;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;

public interface HealthActionEventPublisherPort {
    void publishHealthActionRegistered(HealthAction healthAction);
}
