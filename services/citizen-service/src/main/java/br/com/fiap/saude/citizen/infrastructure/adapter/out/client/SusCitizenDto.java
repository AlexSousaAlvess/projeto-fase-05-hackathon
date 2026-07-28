package br.com.fiap.saude.citizen.infrastructure.adapter.out.client;

import java.time.LocalDate;

public record SusCitizenDto(String cpf, String name, String email, String phone, LocalDate birthDate) {}
