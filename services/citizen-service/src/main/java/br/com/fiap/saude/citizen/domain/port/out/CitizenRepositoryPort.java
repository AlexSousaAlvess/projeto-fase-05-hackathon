package br.com.fiap.saude.citizen.domain.port.out;

import br.com.fiap.saude.citizen.domain.model.Citizen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitizenRepositoryPort {
    Citizen save(Citizen citizen);
    Optional<Citizen> findById(UUID id);
    List<Citizen> findAll();
    boolean existsByCpf(String cpf);
}
