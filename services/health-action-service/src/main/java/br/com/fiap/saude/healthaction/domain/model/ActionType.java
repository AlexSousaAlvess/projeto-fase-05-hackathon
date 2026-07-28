package br.com.fiap.saude.healthaction.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionType {
    VACCINATION(100),
    PREVENTIVE_EXAM(150);

    private final int pointsValue;
}
