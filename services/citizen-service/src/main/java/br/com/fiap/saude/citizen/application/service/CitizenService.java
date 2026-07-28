package br.com.fiap.saude.citizen.application.service;

import br.com.fiap.saude.citizen.domain.model.Citizen;
import br.com.fiap.saude.citizen.domain.model.CitizenStatus;
import br.com.fiap.saude.citizen.domain.port.in.FindCitizenUseCase;
import br.com.fiap.saude.citizen.domain.port.in.SyncCitizensFromSusUseCase;
import br.com.fiap.saude.citizen.domain.port.out.CitizenRepositoryPort;
import br.com.fiap.saude.citizen.domain.port.out.SusClientPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitizenService implements SyncCitizensFromSusUseCase, FindCitizenUseCase {

    private final CitizenRepositoryPort repository;
    private final SusClientPort susClient;

    @Override
    public SyncResult syncAll() {
        int synced = 0;
        int skipped = 0;
        for (SusClientPort.SusCitizenData data : susClient.fetchCitizens()) {
            if (repository.existsByCpf(data.cpf())) {
                skipped++;
                continue;
            }
            repository.save(Citizen.builder()
                    .id(UUID.randomUUID())
                    .cpf(data.cpf())
                    .name(data.name())
                    .email(data.email())
                    .phone(data.phone())
                    .birthDate(data.birthDate())
                    .status(CitizenStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build());
            synced++;
        }
        return new SyncResult(synced, skipped);
    }

    @Override
    public Citizen findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cidadão não encontrado: " + id));
    }

    @Override
    public List<Citizen> findAll() {
        return repository.findAll();
    }
}
