package br.com.fiap.saude.healthaction.infrastructure.adapter.in.web.dto;

import br.com.fiap.saude.healthaction.domain.model.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterHealthActionRequest(
        @NotNull UUID citizenId,
        @NotNull ActionType actionType,
        @NotBlank String description,
        String proofDocumentUrl
) {}
