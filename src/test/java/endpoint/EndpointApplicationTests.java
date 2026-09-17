package endpoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import endpoint.model.CalculoSalvo;
import endpoint.model.Operacao;
import endpoint.model.ResultadoOperacao;
import endpoint.service.HistoricoCalculoService;

@SpringBootTest
@AutoConfigureMockMvc
class EndpointApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private HistoricoCalculoService historicoCalculoService;
    @Test void contextLoads() { }
    @Test void deveSomarDoisNumeros() throws Exception {
        mockMvc.perform(get("/somar").param("numero1", "2").param("numero2", "3"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.operacao").value("SOMA")).andExpect(jsonPath("$.resultado").value(5.0));
    }
    @Test void deveCalcularUsandoEnumDaOperacao() throws Exception {
        mockMvc.perform(get("/calcular").param("numero1", "3").param("numero2", "4").param("operacao", "MULTIPLICACAO"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resultado").value(12.0));
    }
    @Test void deveRetornarBadRequestQuandoFaltarParametro() throws Exception {
        mockMvc.perform(get("/somar").param("numero1", "2")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Parâmetro obrigatório ausente"));
    }
    @Test void deveRetornarBadRequestAoDividirPorZero() throws Exception {
        mockMvc.perform(get("/dividir").param("numero1", "10").param("numero2", "0"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").value("Não é possível dividir por zero."));
    }
    @Test void deveRemoverCalculoEDepoisRetornarNotFound() throws Exception {
        CalculoSalvo calculo = historicoCalculoService.salvar(new ResultadoOperacao(2, 3, Operacao.SOMA, 5));
        mockMvc.perform(delete("/historico/{id}", calculo.id()).with(httpBasic("admin", "123"))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/historico/{id}", calculo.id()).with(httpBasic("admin", "123"))).andExpect(status().isNotFound());
    }
    @Test void deveRetornarConflictAoSalvarCalculoDuplicado() throws Exception {
        mockMvc.perform(post("/historico").with(httpBasic("usuario", "123")).param("numero1", "10").param("numero2", "2").param("operacao", "DIVISAO"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/historico").with(httpBasic("usuario", "123")).param("numero1", "10").param("numero2", "2").param("operacao", "DIVISAO"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Cálculo duplicado"));
    }
    @Test void deveRetornarNotFoundAoBuscarCalculoInexistente() throws Exception {
        mockMvc.perform(get("/historico/{id}", 99999)).andExpect(status().isNotFound());
    }
    @Test void deveRetornarUnprocessableContentQuandoNumeroPassarDoLimite() throws Exception {
        mockMvc.perform(post("/historico").with(httpBasic("usuario", "123")).param("numero1", "1000001").param("numero2", "2").param("operacao", "SOMA"))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.erro").value("Número fora do limite"));
    }
    @Test void deveRetornarUnauthorizedAoCriarCalculoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/historico").param("numero1", "2").param("numero2", "3").param("operacao", "SOMA"))
                .andExpect(status().isUnauthorized());
    }
    @Test void deveRetornarUnauthorizedAoExcluirSemAutenticacao() throws Exception {
        mockMvc.perform(delete("/historico/{id}", 1)).andExpect(status().isUnauthorized());
    }
    @Test void deveRetornarForbiddenAoExcluirComoUsuarioComum() throws Exception {
        CalculoSalvo calculo = historicoCalculoService.salvar(new ResultadoOperacao(20, 4, Operacao.DIVISAO, 5));
        mockMvc.perform(delete("/historico/{id}", calculo.id()).with(httpBasic("usuario", "123")))
                .andExpect(status().isForbidden());
    }
    @Test void deveAtualizarCalculoComoAdmin() throws Exception {
        CalculoSalvo calculo = historicoCalculoService.salvar(new ResultadoOperacao(20, 5, Operacao.SOMA, 25));
        mockMvc.perform(put("/historico/{id}", calculo.id()).with(httpBasic("admin", "123"))
                .param("numero1", "9").param("numero2", "3").param("operacao", "MULTIPLICACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(calculo.id()))
                .andExpect(jsonPath("$.resultado").value(27.0));
    }
    @Test void deveRetornarNotFoundAoAtualizarCalculoInexistente() throws Exception {
        mockMvc.perform(put("/historico/{id}", 99999).with(httpBasic("admin", "123"))
                .param("numero1", "9").param("numero2", "3").param("operacao", "MULTIPLICACAO"))
                .andExpect(status().isNotFound());
    }
    @Test void deveRetornarForbiddenAoAtualizarComoUsuarioComum() throws Exception {
        mockMvc.perform(put("/historico/{id}", 1).with(httpBasic("usuario", "123"))
                .param("numero1", "9").param("numero2", "3").param("operacao", "MULTIPLICACAO"))
                .andExpect(status().isForbidden());
    }
    @Test void deveRetornarTooManyRequestsQuandoUsuarioLimitadoPassarDoLimite() throws Exception {
        for (int numero1 = 301; numero1 <= 303; numero1++) {
            mockMvc.perform(post("/historico").with(httpBasic("limitado", "123"))
                    .param("numero1", String.valueOf(numero1)).param("numero2", "2").param("operacao", "SOMA"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/historico").with(httpBasic("limitado", "123"))
                .param("numero1", "304").param("numero2", "2").param("operacao", "SOMA"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.erro").value("Limite de requisições excedido"));
    }
}
