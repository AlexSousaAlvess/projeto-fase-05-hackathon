package br.com.fiap.saude.healthaction.domain.port.in;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;

import java.util.List;
import java.util.UUID;

public interface FindHealthActionUseCase {
    List<HealthAction> findByCitizenId(UUID citizenId);
}
