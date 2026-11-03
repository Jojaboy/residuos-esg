package br.com.esg.residuos.dto;

import java.time.LocalDateTime;

public record InfoResponse(String aplicacao, String versao, String ambiente, LocalDateTime dataHora) {
}
