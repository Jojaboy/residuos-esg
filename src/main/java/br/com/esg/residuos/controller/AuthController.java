package br.com.esg.residuos.controller;

import br.com.esg.residuos.dto.LoginRequest;
import br.com.esg.residuos.dto.TokenResponse;
import br.com.esg.residuos.entity.Usuario;
import br.com.esg.residuos.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        Usuario usuario = (Usuario) autenticacao.getPrincipal();
        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer",
                jwtService.getExpiracaoEmSegundos(), usuario.getPerfil());
    }
}
