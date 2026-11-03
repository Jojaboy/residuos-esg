package br.com.esg.residuos.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, String perfil) {
}
