package br.com.esg.residuos;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoTest extends ApiTestBase {

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@esg.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@esg.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSemEmailRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"admin123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/pontos-coleta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/api/pontos-coleta").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointsPublicosDeInfoEHealthFuncionamSemToken() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao").value("residuos-esg"))
                .andExpect(jsonPath("$.ambiente").value("test"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
