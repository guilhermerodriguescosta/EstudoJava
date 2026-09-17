package br.com.guilhermecosta.estudojava;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import br.com.guilhermecosta.estudojava.model.CalculoSalvo;
import br.com.guilhermecosta.estudojava.model.Operacao;
import br.com.guilhermecosta.estudojava.model.ResultadoOperacao;
import br.com.guilhermecosta.estudojava.service.HistoricoCalculoService;

@SpringBootTest
@AutoConfigureMockMvc
class EstudoJavaApplicationTests {
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
        mockMvc.perform(delete("/historico/{id}", calculo.id())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/historico/{id}", calculo.id())).andExpect(status().isNotFound());
    }
    @Test void deveRetornarConflictAoSalvarCalculoDuplicado() throws Exception {
        mockMvc.perform(post("/historico").param("numero1", "10").param("numero2", "2").param("operacao", "DIVISAO"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/historico").param("numero1", "10").param("numero2", "2").param("operacao", "DIVISAO"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Cálculo duplicado"));
    }
}
