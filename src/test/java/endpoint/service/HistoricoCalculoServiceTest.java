package endpoint.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import endpoint.exception.CalculoDuplicadoException;
import endpoint.model.Operacao;
import endpoint.model.ResultadoOperacao;
import endpoint.repository.CalculoRepository;

class HistoricoCalculoServiceTest {
    private final CalculoRepository repository = new CalculoRepository();
    private final HistoricoCalculoService service = new HistoricoCalculoService(repository);

    @Test
    void deveSalvarConsultarAtualizarERemoverUsandoRepository() {
        var salvo = service.salvar(new ResultadoOperacao(2, 3, Operacao.SOMA, 5));
        assertEquals(salvo, repository.buscarPorId(salvo.id()).orElseThrow());
        assertEquals(salvo, service.buscarPorId(salvo.id()).orElseThrow());

        var atualizado = service
                .atualizar(salvo.id(), new ResultadoOperacao(4, 3, Operacao.SOMA, 7)).orElseThrow();
        assertEquals(salvo.id(), atualizado.id());
        assertEquals(7, repository.buscarPorId(salvo.id()).orElseThrow().resultado());
        assertTrue(service.removerPorId(salvo.id()));
        assertTrue(repository.buscarPorId(salvo.id()).isEmpty());
        assertFalse(service.removerPorId(salvo.id()));
    }

    @Test
    void deveImpedirDuplicidadeNaCriacaoENaAtualizacao() {
        var primeiro = new ResultadoOperacao(2, 3, Operacao.SOMA, 5);
        var segundo = new ResultadoOperacao(4, 3, Operacao.SOMA, 7);
        var salvo = service.salvar(primeiro);
        var outro = service.salvar(segundo);

        assertThrows(CalculoDuplicadoException.class, () -> service.salvar(primeiro));
        assertThrows(CalculoDuplicadoException.class,
                () -> service.atualizar(outro.id(), primeiro));
        assertEquals(7, service.buscarPorId(outro.id()).orElseThrow().resultado());
        assertTrue(service.atualizar(salvo.id(), primeiro).isPresent());
    }

    @Test
    void naoDeveCriarCalculoAoAtualizarIdInexistente() {
        assertTrue(service.atualizar(99, new ResultadoOperacao(2, 3, Operacao.SOMA, 5)).isEmpty());
        assertTrue(repository.buscarTodos().isEmpty());
    }
}
