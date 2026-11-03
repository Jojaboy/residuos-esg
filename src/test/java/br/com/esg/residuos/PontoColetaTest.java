package br.com.esg.residuos;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PontoColetaTest extends ApiTestBase {

    private static final String NOVO_PONTO = """
            {
              "nome": "Ecoponto Teste",
              "endereco": "Av. Atlantica, 1000",
              "bairro": "Copacabana",
              "tipoResiduo": "METAL",
              "capacidadeKg": 250.00
            }
            """;

    @Test
    void listaPontosDoSeed() throws Exception {
        mockMvc.perform(get("/api/pontos-coleta").header("Authorization", tokenOperador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void filtraPorTipoDeResiduo() throws Exception {
        mockMvc.perform(get("/api/pontos-coleta").param("tipo", "VIDRO").header("Authorization", tokenOperador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipoResiduo", everyItem(is("VIDRO"))));
    }

    @Test
    void criaBuscaEAtualizaPonto() throws Exception {
        String token = tokenAdmin();

        MvcResult criado = mockMvc.perform(post("/api/pontos-coleta")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PONTO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ecoponto Teste"))
                .andReturn();
        long id = json(criado).get("id").asLong();

        mockMvc.perform(get("/api/pontos-coleta/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairro").value("Copacabana"));

        mockMvc.perform(put("/api/pontos-coleta/" + id)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PONTO.replace("Copacabana", "Leblon")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairro").value("Leblon"));
    }

    @Test
    void pontoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/pontos-coleta/999999").header("Authorization", tokenAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void dadosInvalidosRetornam400ComDetalhes() throws Exception {
        mockMvc.perform(post("/api/pontos-coleta")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"capacidadeKg\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    void operadorNaoPodeExcluir() throws Exception {
        mockMvc.perform(delete("/api/pontos-coleta/1").header("Authorization", tokenOperador()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminExcluiPontoSemColetas() throws Exception {
        String token = tokenAdmin();
        MvcResult criado = mockMvc.perform(post("/api/pontos-coleta")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PONTO))
                .andExpect(status().isCreated())
                .andReturn();
        long id = json(criado).get("id").asLong();

        mockMvc.perform(delete("/api/pontos-coleta/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pontos-coleta/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiPontoComColetasRegistradas() throws Exception {
        String token = tokenAdmin();
        MvcResult lista = mockMvc.perform(get("/api/pontos-coleta").param("tipo", "PLASTICO")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        long idComColetas = json(lista).get(0).get("id").asLong();

        mockMvc.perform(delete("/api/pontos-coleta/" + idComColetas).header("Authorization", token))
                .andExpect(status().isConflict());
    }
}
