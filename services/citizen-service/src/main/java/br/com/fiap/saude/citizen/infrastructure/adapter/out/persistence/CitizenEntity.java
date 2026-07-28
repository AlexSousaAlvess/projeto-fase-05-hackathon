package br.com.fiap.saude.citizen.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.citizen.domain.model.CitizenStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "citizens")
@Getter @Setter @NoArgsConstructor
public class CitizenEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private CitizenStatus status;

    private LocalDateTime createdAt;
}
