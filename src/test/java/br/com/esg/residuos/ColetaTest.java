package br.com.esg.residuos;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ColetaTest extends ApiTestBase {

    private long primeiroPontoId(String token) throws Exception {
        MvcResult lista = mockMvc.perform(get("/api/pontos-coleta").header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        return json(lista).get(0).get("id").asLong();
    }

    private long criarColeta(String token, long pontoId) throws Exception {
        String corpo = """
                {"pontoColetaId": %d, "dataColeta": "2026-11-05", "quantidadeKg": 75.5, "observacao": "Teste"}
                """.formatted(pontoId);
        MvcResult criada = mockMvc.perform(post("/api/coletas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.dataColeta").value("2026-11-05"))
                .andReturn();
        return json(criada).get("id").asLong();
    }

    @Test
    void criaEConcluiColeta() throws Exception {
        String token = tokenOperador();
        long id = criarColeta(token, primeiroPontoId(token));

        mockMvc.perform(patch("/api/coletas/" + id + "/concluir").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // regra de negocio: nao conclui duas vezes
        mockMvc.perform(patch("/api/coletas/" + id + "/concluir").header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void coletaParaPontoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/coletas")
                        .header("Authorization", tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontoColetaId\": 999999, \"dataColeta\": \"2026-11-05\", \"quantidadeKg\": 10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void indicadoresSomamApenasColetasConcluidas() throws Exception {
        String token = tokenAdmin();

        JsonNode antes = json(mockMvc.perform(get("/api/indicadores").header("Authorization", token))
                .andExpect(status().isOk()).andReturn());
        BigDecimal kgAntes = antes.get("totalKgReciclado").decimalValue();
        // seed: 120.50 + 340.00 kg concluidos
        assertThat(kgAntes).isGreaterThanOrEqualTo(new BigDecimal("460.50"));

        long id = criarColeta(token, primeiroPontoId(token));
        JsonNode comAgendada = json(mockMvc.perform(get("/api/indicadores").header("Authorization", token))
                .andReturn());
        assertThat(comAgendada.get("totalKgReciclado").decimalValue()).isEqualByComparingTo(kgAntes);

        mockMvc.perform(patch("/api/coletas/" + id + "/concluir").header("Authorization", token))
                .andExpect(status().isOk());
        JsonNode depois = json(mockMvc.perform(get("/api/indicadores").header("Authorization", token))
                .andReturn());
        assertThat(depois.get("totalKgReciclado").decimalValue())
                .isEqualByComparingTo(kgAntes.add(new BigDecimal("75.5")));
    }
}
