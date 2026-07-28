package br.com.fiap.saude.citizen.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Citizen {
    private UUID id;
    private String cpf;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private CitizenStatus status;
    private LocalDateTime createdAt;
}
