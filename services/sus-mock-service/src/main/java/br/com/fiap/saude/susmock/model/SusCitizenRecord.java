package br.com.fiap.saude.susmock.model;

import java.time.LocalDate;

public record SusCitizenRecord(
        String cpf,
        String name,
        String email,
        String phone,
        LocalDate birthDate
) {}
