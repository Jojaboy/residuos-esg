package br.com.esg.residuos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integracao: sobe a aplicacao inteira com banco H2 em memoria
 * (perfil "test") e desfaz as alteracoes no banco ao fim de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestBase {

    protected static final String ADMIN = "admin@esg.com";
    protected static final String SENHA_ADMIN = "admin123";
    protected static final String OPERADOR = "operador@esg.com";
    protected static final String SENHA_OPERADOR = "operador123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String tokenDe(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + json(resultado).get("token").asText();
    }

    protected String tokenAdmin() throws Exception {
        return tokenDe(ADMIN, SENHA_ADMIN);
    }

    protected String tokenOperador() throws Exception {
        return tokenDe(OPERADOR, SENHA_OPERADOR);
    }

    protected JsonNode json(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }
}
