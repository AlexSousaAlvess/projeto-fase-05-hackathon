package br.com.fiap.saude.points.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PointTransaction {
    private UUID id;
    private UUID accountId;
    private TransactionType type;
    private int amount;
    private String reason;
    private UUID referenceId;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
