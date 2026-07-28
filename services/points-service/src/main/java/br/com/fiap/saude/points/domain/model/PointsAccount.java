package br.com.fiap.saude.points.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PointsAccount {
    private UUID id;
    private UUID citizenId;
    private int balance;
    private int totalEarned;
    private int totalRedeemed;
    private LocalDateTime updatedAt;

    public boolean hasSufficientBalance(int amount) {
        return this.balance >= amount;
    }
}
