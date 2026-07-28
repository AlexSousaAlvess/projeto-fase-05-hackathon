package br.com.fiap.saude.citizen.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.citizen.domain.model.Citizen;
import br.com.fiap.saude.citizen.domain.port.out.CitizenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CitizenPersistenceAdapter implements CitizenRepositoryPort {

    private final CitizenJpaRepository jpaRepository;

    @Override
    public Citizen save(Citizen citizen) {
        CitizenEntity entity = toEntity(citizen);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Citizen> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Citizen> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    private CitizenEntity toEntity(Citizen citizen) {
        CitizenEntity entity = new CitizenEntity();
        entity.setId(citizen.getId());
        entity.setCpf(citizen.getCpf());
        entity.setName(citizen.getName());
        entity.setEmail(citizen.getEmail());
        entity.setPhone(citizen.getPhone());
        entity.setBirthDate(citizen.getBirthDate());
        entity.setStatus(citizen.getStatus());
        entity.setCreatedAt(citizen.getCreatedAt());
        return entity;
    }

    private Citizen toDomain(CitizenEntity entity) {
        return Citizen.builder()
                .id(entity.getId())
                .cpf(entity.getCpf())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .birthDate(entity.getBirthDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
