package br.com.fiap.saude.points.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "points_accounts")
@Getter @Setter @NoArgsConstructor
public class PointsAccountEntity {
    @Id
    private UUID id;
    @Column(unique = true, nullable = false)
    private UUID citizenId;
    private int balance;
    private int totalEarned;
    private int totalRedeemed;
    private LocalDateTime updatedAt;
}
