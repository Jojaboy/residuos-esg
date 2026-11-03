package br.com.esg.bdd.steps;

import br.com.esg.bdd.Configuracao;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passos do Gherkin. O Cucumber cria uma instancia desta classe por cenario, entao os
 * campos abaixo nunca vazam de um cenario para outro.
 */
public class ApiSteps {

    private static final String JSON = "application/json; charset=UTF-8";
    private static final String PONTOS = "/api/pontos-coleta";
    private static final String COLETAS = "/api/coletas";
    private static final String INDICADORES = "/api/indicadores";

    private String token;
    private String tokenAdmin;
    private Response resposta;
    private Response indicadoresAntes;
    private Long pontoId;
    private Long coletaId;
    private final Deque<Long> pontosCriados = new ArrayDeque<>();
    private final Deque<Long> coletasCriadas = new ArrayDeque<>();

    // ------------------------------------------------------------------ Dado

    @Given("que a API está no ar")
    public void queAApiEstaNoAr() {
        requisicao(null).get("/actuator/health").then().statusCode(200);
    }

    @Given("que estou autenticado como {string}")
    public void queEstouAutenticadoComo(String perfil) {
        token = switch (perfil) {
            case "ADMIN" -> login(Configuracao.ADMIN_EMAIL, Configuracao.ADMIN_SENHA);
            case "OPERADOR" -> login(Configuracao.OPERADOR_EMAIL, Configuracao.OPERADOR_SENHA);
            default -> throw new IllegalArgumentException("Perfil desconhecido: " + perfil);
        };
    }

    @Given("que não estou autenticado")
    public void queNaoEstouAutenticado() {
        token = null;
    }

    @Given("que estou usando um token inválido")
    public void queEstouUsandoUmTokenInvalido() {
        token = "token.invalido.bdd";
    }

    @Given("que existe um ponto de coleta cadastrado")
    public void queExisteUmPontoDeColetaCadastrado() {
        String corpo = """
                {"nome": "Ponto BDD %s", "endereco": "Rua dos Testes, 100", "bairro": "Centro",
                 "tipoResiduo": "METAL", "capacidadeKg": 250.00}
                """.formatted(UUID.randomUUID().toString().substring(0, 8));
        Response criado = requisicao(tokenAdmin()).contentType(JSON).body(corpo).post(PONTOS);
        assertEquals(201, criado.statusCode(), "Falha na preparacao do ponto de coleta");
        pontoId = criado.jsonPath().getLong("id");
        pontosCriados.push(pontoId);
    }

    @Given("que existe uma coleta agendada de {string} kg nesse ponto")
    public void queExisteUmaColetaAgendada(String quantidadeKg) {
        String corpo = """
                {"pontoColetaId": %d, "dataColeta": "2026-11-10", "quantidadeKg": %s,
                 "observacao": "Coleta criada pelos testes BDD"}
                """.formatted(pontoId, quantidadeKg);
        Response criada = requisicao(tokenAdmin()).contentType(JSON).body(corpo).post(COLETAS);
        assertEquals(201, criada.statusCode(), "Falha na preparacao da coleta");
        coletaId = criada.jsonPath().getLong("id");
        coletasCriadas.push(coletaId);
    }

    @Given("que essa coleta já foi concluída")
    public void queEssaColetaJaFoiConcluida() {
        Response concluida = requisicao(tokenAdmin()).patch(COLETAS + "/" + coletaId + "/concluir");
        assertEquals(200, concluida.statusCode(), "Falha na preparacao da coleta concluida");
    }

    @Given("que registrei os indicadores atuais")
    public void queRegistreiOsIndicadoresAtuais() {
        indicadoresAntes = requisicao(tokenAdmin()).get(INDICADORES);
        assertEquals(200, indicadoresAntes.statusCode(), "Falha ao consultar os indicadores");
    }

    // ---------------------------------------------------------------- Quando

    @When("envio uma requisição {word} para {string}")
    public void envioUmaRequisicao(String metodo, String caminho) {
        enviar(metodo, caminho, null);
    }

    @When("envio uma requisição {word} para {string} com o corpo:")
    public void envioUmaRequisicaoComCorpo(String metodo, String caminho, String corpo) {
        enviar(metodo, caminho, corpo);
    }

    @When("faço login com e-mail {string} e senha {string}")
    public void facoLogin(String email, String senha) {
        String corpo = "{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha);
        enviar("POST", "/api/auth/login", corpo);
    }

    @When("consulto novamente os indicadores")
    public void consultoNovamenteOsIndicadores() {
        resposta = requisicao(tokenAdmin()).get(INDICADORES);
        assertEquals(200, resposta.statusCode(), "Falha ao consultar os indicadores");
    }

    // ----------------------------------------------------------------- Entao

    @Then("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int status) {
        assertEquals(status, resposta.statusCode(), "Corpo recebido: " + resposta.asString());
    }

    @Then("o corpo da resposta deve seguir o contrato {string}")
    public void oCorpoDeveSeguirOContrato(String arquivo) {
        resposta.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/" + arquivo));
    }

    @Then("o campo {string} deve ser {string}")
    public void oCampoDeveSer(String campo, String esperado) {
        assertEquals(esperado, resposta.jsonPath().getString(campo));
    }

    @Then("o campo {string} deve ser o número {string}")
    public void oCampoDeveSerONumero(String campo, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(numero(resposta, campo)),
                "Valor recebido em " + campo + ": " + numero(resposta, campo));
    }

    @Then("o campo {string} deve estar preenchido")
    public void oCampoDeveEstarPreenchido(String campo) {
        String valor = resposta.jsonPath().getString(campo);
        assertNotNull(valor, "Campo ausente: " + campo);
        assertFalse(valor.isBlank(), "Campo vazio: " + campo);
    }

    @Then("a resposta deve ser uma lista com pelo menos {int} itens")
    public void aRespostaDeveSerUmaLista(int minimo) {
        List<Object> itens = resposta.jsonPath().getList("$");
        assertTrue(itens.size() >= minimo, "A lista tem " + itens.size() + " itens");
    }

    @Then("todos os itens devem ter o campo {string} igual a {string}")
    public void todosOsItensDevemTer(String campo, String esperado) {
        List<String> valores = resposta.jsonPath().getList(campo, String.class);
        assertFalse(valores.isEmpty(), "A lista veio vazia");
        valores.forEach(valor -> assertEquals(esperado, valor));
    }

    @Then("a lista deve conter a coleta criada")
    public void aListaDeveConterAColetaCriada() {
        List<Long> ids = resposta.jsonPath().getList("id", Long.class);
        assertTrue(ids.contains(coletaId), "Coleta " + coletaId + " nao encontrada em " + ids);
    }

    @Then("todas as coletas devem pertencer ao ponto criado")
    public void todasAsColetasDevemPertencerAoPontoCriado() {
        List<Long> ids = resposta.jsonPath().getList("pontoColetaId", Long.class);
        assertFalse(ids.isEmpty(), "A lista veio vazia");
        ids.forEach(id -> assertEquals(pontoId, id));
    }

    @Then("o campo {string} deve ser o identificador do ponto criado")
    public void oCampoDeveSerOIdentificadorDoPontoCriado(String campo) {
        assertEquals(pontoId, resposta.jsonPath().getLong(campo));
    }

    @Then("o cabeçalho {string} deve estar preenchido")
    public void oCabecalhoDeveEstarPreenchido(String cabecalho) {
        String valor = resposta.getHeader(cabecalho);
        assertNotNull(valor, "Cabecalho ausente: " + cabecalho);
        assertFalse(valor.isBlank(), "Cabecalho vazio: " + cabecalho);
    }

    @Then("a mensagem de erro deve conter {string}")
    public void aMensagemDeErroDeveConter(String trecho) {
        String mensagem = resposta.jsonPath().getString("mensagem");
        assertNotNull(mensagem, "Resposta sem mensagem: " + resposta.asString());
        assertTrue(mensagem.contains(trecho), "Mensagem recebida: " + mensagem);
    }

    @Then("os detalhes do erro devem citar o campo {string}")
    public void osDetalhesDoErroDevemCitarOCampo(String campo) {
        List<String> detalhes = resposta.jsonPath().getList("detalhes", String.class);
        assertTrue(detalhes.stream().anyMatch(d -> d.startsWith(campo + ":")), "Detalhes recebidos: " + detalhes);
    }

    @Then("o corpo da resposta deve estar vazio")
    public void oCorpoDaRespostaDeveEstarVazio() {
        assertEquals("", resposta.asString());
    }

    @Then("o registro não deve mais existir em {string}")
    public void oRegistroNaoDeveMaisExistirEm(String caminho) {
        Response consulta = requisicao(tokenAdmin()).get(resolver(caminho));
        assertEquals(404, consulta.statusCode());
    }

    @Then("o indicador {string} deve ter aumentado em {string}")
    public void oIndicadorDeveTerAumentadoEm(String indicador, String aumento) {
        BigDecimal diferenca = numero(resposta, indicador).subtract(numero(indicadoresAntes, indicador));
        assertEquals(0, new BigDecimal(aumento).compareTo(diferenca), "Diferenca em " + indicador + ": " + diferenca);
    }

    @Then("o indicador {string} deve ter permanecido igual")
    public void oIndicadorDeveTerPermanecidoIgual(String indicador) {
        assertEquals(0, numero(indicadoresAntes, indicador).compareTo(numero(resposta, indicador)),
                "O indicador " + indicador + " mudou");
    }

    // ----------------------------------------------------------------- Apoio

    /** Remove o que o cenario criou, para os testes poderem rodar varias vezes no mesmo banco. */
    @After
    public void limparDadosDoCenario() {
        if (coletasCriadas.isEmpty() && pontosCriados.isEmpty()) {
            return;
        }
        coletasCriadas.forEach(id -> requisicao(tokenAdmin()).delete(COLETAS + "/" + id));
        pontosCriados.forEach(id -> requisicao(tokenAdmin()).delete(PONTOS + "/" + id));
    }

    private void enviar(String metodo, String caminho, String corpo) {
        RequestSpecification requisicao = requisicao(token);
        if (corpo != null) {
            requisicao = requisicao.contentType(JSON).body(resolver(corpo));
        }
        resposta = requisicao.request(metodo, resolver(caminho));
        registrarCriacao(metodo, caminho);
    }

    /** Guarda o id criado pelo proprio cenario, para usar nos proximos passos e na limpeza. */
    private void registrarCriacao(String metodo, String caminho) {
        if (!"POST".equals(metodo) || resposta.statusCode() != 201) {
            return;
        }
        if (caminho.equals(PONTOS)) {
            pontoId = resposta.jsonPath().getLong("id");
            pontosCriados.push(pontoId);
        } else if (caminho.equals(COLETAS)) {
            coletaId = resposta.jsonPath().getLong("id");
            coletasCriadas.push(coletaId);
        }
    }

    private String resolver(String texto) {
        return texto.replace("{pontoId}", String.valueOf(pontoId))
                .replace("{coletaId}", String.valueOf(coletaId));
    }

    private RequestSpecification requisicao(String tokenDaRequisicao) {
        RequestSpecification requisicao = RestAssured.given().baseUri(Configuracao.BASE_URL);
        if (tokenDaRequisicao != null) {
            requisicao = requisicao.header("Authorization", "Bearer " + tokenDaRequisicao);
        }
        return requisicao;
    }

    private String tokenAdmin() {
        if (tokenAdmin == null) {
            tokenAdmin = login(Configuracao.ADMIN_EMAIL, Configuracao.ADMIN_SENHA);
        }
        return tokenAdmin;
    }

    private String login(String email, String senha) {
        String corpo = "{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha);
        Response login = requisicao(null).contentType(JSON).body(corpo).post("/api/auth/login");
        assertEquals(200, login.statusCode(), "Falha no login de " + email + ": " + login.asString());
        return login.jsonPath().getString("token");
    }

    private BigDecimal numero(Response origem, String campo) {
        JsonPathConfig configuracao = JsonPathConfig.jsonPathConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
        return new BigDecimal(origem.jsonPath(configuracao).getString(campo));
    }
}
