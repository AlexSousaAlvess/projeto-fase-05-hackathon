package br.com.fiap.saude.healthaction.domain.port.in;

import br.com.fiap.saude.healthaction.domain.model.ActionType;
import br.com.fiap.saude.healthaction.domain.model.HealthAction;

import java.util.UUID;

public interface RegisterHealthActionUseCase {

    HealthAction register(Command command);

    record Command(UUID citizenId, ActionType actionType, String description, String proofDocumentUrl) {}
}
