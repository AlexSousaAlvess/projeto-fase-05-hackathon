package br.com.fiap.saude.susmock;

import br.com.fiap.saude.susmock.model.SusCitizenRecord;

import java.time.LocalDate;
import java.util.List;

// Stand-in for the national SUS citizen registry (CADSUS) — fixed dataset, no real integration.
public final class SusCitizenSeedData {

    public static final List<SusCitizenRecord> CITIZENS = List.of(
            new SusCitizenRecord("12345678900", "Maria Oliveira Santos", "maria.santos@example.com", "11988887777", LocalDate.of(1985, 3, 12)),
            new SusCitizenRecord("23456789011", "João Pereira Costa", "joao.costa@example.com", "11977776666", LocalDate.of(1990, 7, 25)),
            new SusCitizenRecord("34567890122", "Ana Beatriz Lima", "ana.lima@example.com", "21966665555", LocalDate.of(1978, 11, 2)),
            new SusCitizenRecord("45678901233", "Carlos Eduardo Souza", "carlos.souza@example.com", "21955554444", LocalDate.of(1995, 1, 30)),
            new SusCitizenRecord("56789012344", "Fernanda Almeida Rocha", "fernanda.rocha@example.com", "31944443333", LocalDate.of(1988, 9, 17)),
            new SusCitizenRecord("67890123455", "Pedro Henrique Martins", "pedro.martins@example.com", "31933332222", LocalDate.of(2000, 5, 8)),
            new SusCitizenRecord("78901234566", "Juliana Ferreira Dias", "juliana.dias@example.com", "41922221111", LocalDate.of(1972, 4, 21)),
            new SusCitizenRecord("89012345677", "Rafael Gonçalves Barros", "rafael.barros@example.com", "41911110000", LocalDate.of(1993, 12, 14))
    );

    private SusCitizenSeedData() {
    }
}
