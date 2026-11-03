package br.com.esg.residuos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ColetaRequest(
        @NotNull Long pontoColetaId,
        @NotNull LocalDate dataColeta,
        @NotNull @DecimalMin("0.1") BigDecimal quantidadeKg,
        @Size(max = 255) String observacao) {
}
