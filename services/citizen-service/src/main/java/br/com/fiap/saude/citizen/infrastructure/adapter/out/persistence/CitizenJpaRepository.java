package br.com.fiap.saude.citizen.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitizenJpaRepository extends JpaRepository<CitizenEntity, UUID> {
    boolean existsByCpf(String cpf);
}
