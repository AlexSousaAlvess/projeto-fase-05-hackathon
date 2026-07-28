package br.com.fiap.saude.points.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.points.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_transactions")
@Getter @Setter @NoArgsConstructor
public class PointTransactionEntity {
    @Id
    private UUID id;
    private UUID accountId;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private int amount;
    private String reason;
    private UUID referenceId;
    @Column(unique = true)
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
