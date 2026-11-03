package br.com.esg.residuos.dto;

import br.com.esg.residuos.entity.TipoResiduo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PontoColetaRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 200) String endereco,
        @NotBlank @Size(max = 80) String bairro,
        @NotNull TipoResiduo tipoResiduo,
        @NotNull @DecimalMin("0.1") BigDecimal capacidadeKg) {
}
