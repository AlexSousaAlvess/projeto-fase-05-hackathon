package br.com.fiap.saude.citizen.infrastructure.adapter.in.web.dto;

import br.com.fiap.saude.citizen.domain.model.Citizen;
import br.com.fiap.saude.citizen.domain.model.CitizenStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CitizenResponse(
        UUID id,
        String cpf,
        String name,
        String email,
        String phone,
        LocalDate birthDate,
        CitizenStatus status,
        LocalDateTime createdAt
) {
    public static CitizenResponse from(Citizen citizen) {
        return new CitizenResponse(
                citizen.getId(), citizen.getCpf(), citizen.getName(),
                citizen.getEmail(), citizen.getPhone(), citizen.getBirthDate(),
                citizen.getStatus(), citizen.getCreatedAt()
        );
    }
}
