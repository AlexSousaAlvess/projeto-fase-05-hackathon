package br.com.fiap.saude.healthaction.domain.port.out;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;

import java.util.List;
import java.util.UUID;

public interface HealthActionRepositoryPort {
    HealthAction save(HealthAction healthAction);
    List<HealthAction> findByCitizenId(UUID citizenId);
}
