package endpoint.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import endpoint.exception.CalculoDuplicadoException;
import endpoint.model.CalculoSalvo;
import endpoint.model.Operacao;
import endpoint.model.ResultadoOperacao;
import endpoint.repository.CalculoRepository;

class HistoricoCalculoServiceTest {
    private final CalculoRepository repository = mock(CalculoRepository.class);
    private final HistoricoCalculoService service = new HistoricoCalculoService(repository);

    @Test
    void deveDelegarCrudAoRepository() {
        var resultado = new ResultadoOperacao(2, 3, Operacao.SOMA, 5);
        var salvo = new CalculoSalvo(7, 2, 3, Operacao.SOMA, 5);
        when(repository.salvar(resultado)).thenReturn(salvo);
        when(repository.buscarPorId(7)).thenReturn(Optional.of(salvo));
        when(repository.atualizar(7, resultado)).thenReturn(Optional.of(salvo));
        when(repository.removerPorId(7)).thenReturn(true);
        assertEquals(salvo, service.salvar(resultado));
        assertEquals(Optional.of(salvo), service.buscarPorId(7));
        assertEquals(Optional.of(salvo), service.atualizar(7, resultado));
        assertTrue(service.removerPorId(7));
    }

    @Test
    void devePropagarDuplicidadeSemAlterarContrato() {
        var resultado = new ResultadoOperacao(2, 3, Operacao.SOMA, 5);
        when(repository.salvar(resultado)).thenThrow(new CalculoDuplicadoException());
        assertThrows(CalculoDuplicadoException.class, () -> service.salvar(resultado));
    }
}
