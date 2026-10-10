package endpoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import endpoint.exception.CalculoDuplicadoException;
import endpoint.model.*;
import endpoint.persistence.CalculoJpaRepository;
import endpoint.repository.CalculoRepository;
import endpoint.repository.PedidoRepository;
import endpoint.service.PedidoFilaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@Import(PostgresTestConfiguration.class)
class PersistenciaPostgresTests {
    @Autowired CalculoRepository calculos;
    @Autowired PedidoRepository pedidos;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean CalculoJpaRepository calculosJpa;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("TRUNCATE TABLE calculos, pedidos RESTART IDENTITY");
    }

    @Test
    void devePersistirCrudEImpedirDuplicidade() {
        var primeiro = new ResultadoOperacao(2, 3, Operacao.SOMA, 5);
        var segundo = new ResultadoOperacao(4, 3, Operacao.SOMA, 7);
        var salvo = calculos.salvar(primeiro);
        var outro = calculos.salvar(segundo);
        assertTrue(salvo.id() > 0);
        assertNotEquals(salvo.id(), outro.id());
        assertEquals(salvo, calculos.buscarPorId(salvo.id()).orElseThrow());
        assertThrows(CalculoDuplicadoException.class, () -> calculos.salvar(primeiro));
        assertThrows(CalculoDuplicadoException.class, () -> calculos.atualizar(outro.id(), primeiro));
        assertEquals(7, calculos.buscarPorId(outro.id()).orElseThrow().resultado());
        assertEquals(salvo, calculos.atualizar(salvo.id(), primeiro).orElseThrow());
        var atualizado = calculos.atualizar(salvo.id(), new ResultadoOperacao(8, 3, Operacao.SOMA, 11)).orElseThrow();
        assertEquals(salvo.id(), atualizado.id());
        assertEquals(11, atualizado.resultado());
        assertTrue(calculos.removerPorId(salvo.id()));
        assertFalse(calculos.removerPorId(salvo.id()));
        assertTrue(calculos.buscarPorId(salvo.id()).isEmpty());
        assertTrue(calculos.atualizar(99999, primeiro).isEmpty());
        assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM calculos", Long.class));
    }

    @Test
    void deveImpedirInsercoesConcorrentesPelaRestricaoDoBanco() throws Exception {
        var barreira = new CyclicBarrier(2);
        // Garante que ambas as consultas vejam a ausencia antes de qualquer INSERT.
        doAnswer(invocacao -> {
            boolean existe = jdbc.queryForObject("""
                    SELECT EXISTS (SELECT 1 FROM calculos
                    WHERE numero1 = ? AND numero2 = ? AND operacao = ?)
                    """, Boolean.class, 22, 3, "SOMA");
            barreira.await(20, TimeUnit.SECONDS);
            return existe;
        }).when(calculosJpa).existsByNumero1AndNumero2AndOperacao(22, 3, Operacao.SOMA);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> inserir = () -> {
                try {
                    calculos.salvar(new ResultadoOperacao(22, 3, Operacao.SOMA, 25));
                    return true;
                } catch (CalculoDuplicadoException esperado) {
                    return false;
                }
            };
            var a = executor.submit(inserir);
            var b = executor.submit(inserir);
            assertNotEquals(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
        }
        assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM calculos", Long.class));
    }

    @Test
    void naoDeveTraduzirOutraRestricaoComoDuplicidade() {
        assertThrows(DataIntegrityViolationException.class,
                () -> calculos.salvar(new ResultadoOperacao(2, 3, null, 5)));
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM calculos", Long.class));
    }

    @Test
    void devePersistirPedidoAntesDaPublicacaoEProcessarDeFormaIdempotente() {
        var rabbit = mock(RabbitTemplate.class);
        doAnswer(invocacao -> {
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
            var mensagem = (PedidoMensagem) invocacao.getArgument(1);
            assertEquals("PENDING", jdbc.queryForObject("SELECT status FROM pedidos WHERE id = ?",
                    String.class, mensagem.id()));
            return null;
        }).when(rabbit).convertAndSend(anyString(), any(Object.class));
        var service = new PedidoFilaService(rabbit, pedidos);
        var id = service.enviar("Pedido persistente");
        service.marcarComoProcessado(id);
        service.marcarComoProcessado(id);
        var salvo = pedidos.buscarPorId(id).orElseThrow();
        assertEquals(PedidoStatus.PROCESSED, salvo.status());
        assertEquals("Pedido persistente", salvo.descricao());
        var inexistente = UUID.randomUUID();
        service.marcarComoProcessado(inexistente);
        assertTrue(pedidos.buscarPorId(inexistente).isEmpty());
    }

    @Test
    void falhaDeGravacaoNaoDevePublicarPedido() {
        var rabbit = mock(RabbitTemplate.class);
        var service = new PedidoFilaService(rabbit, pedidos);
        assertThrows(DataIntegrityViolationException.class, () -> service.enviar(null));
        verifyNoInteractions(rabbit);
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM pedidos", Long.class));
    }

    @Test
    void falhaDePublicacaoDevePreservarPedidoPendente() {
        var rabbit = mock(RabbitTemplate.class);
        var id = new AtomicReference<UUID>();
        doAnswer(invocacao -> {
            id.set(((PedidoMensagem) invocacao.getArgument(1)).id());
            throw new AmqpException("Fila indisponivel");
        }).when(rabbit).convertAndSend(anyString(), any(Object.class));
        var service = new PedidoFilaService(rabbit, pedidos);
        assertThrows(AmqpException.class, () -> service.enviar("Pedido pendente"));
        assertEquals(PedidoStatus.PENDING, pedidos.buscarPorId(id.get()).orElseThrow().status());
    }
}
