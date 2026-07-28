package br.com.fiap.saude.citizen.domain.port.in;

import br.com.fiap.saude.citizen.domain.model.Citizen;

import java.util.List;
import java.util.UUID;

public interface FindCitizenUseCase {
    Citizen findById(UUID id);

    List<Citizen> findAll();
}
