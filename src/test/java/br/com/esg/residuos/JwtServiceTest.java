package br.com.esg.residuos;

import br.com.esg.residuos.entity.Usuario;
import br.com.esg.residuos.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Teste unitario puro (sem Spring) do servico de tokens JWT. */
class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-unitario-com-mais-de-32-caracteres";

    private final JwtService jwtService = new JwtService(SEGREDO, 60_000);

    @Test
    void geraTokenComEmailDoUsuario() {
        Usuario usuario = new Usuario("Teste", "teste@esg.com", "hash", "ADMIN");

        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.tokenValido(token)).isTrue();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("teste@esg.com");
    }

    @Test
    void rejeitaTokenAdulterado() {
        String token = jwtService.gerarToken(new Usuario("Teste", "teste@esg.com", "hash", "ADMIN"));

        int inicioDoConteudo = token.indexOf('.') + 1;
        char trocado = token.charAt(inicioDoConteudo) == 'a' ? 'b' : 'a';
        String adulterado = token.substring(0, inicioDoConteudo) + trocado + token.substring(inicioDoConteudo + 1);

        assertThat(jwtService.tokenValido(adulterado)).isFalse();
        assertThat(jwtService.tokenValido("nao-e-um-jwt")).isFalse();
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        JwtService outro = new JwtService("outra-chave-totalmente-diferente-com-32-caracteres", 60_000);
        String token = outro.gerarToken(new Usuario("Teste", "teste@esg.com", "hash", "ADMIN"));

        assertThat(jwtService.tokenValido(token)).isFalse();
    }

    @Test
    void exigeSegredoComPeloMenos32Caracteres() {
        assertThatThrownBy(() -> new JwtService("curto", 60_000))
                .isInstanceOf(IllegalStateException.class);
    }
}
