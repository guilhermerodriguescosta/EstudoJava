package endpoint.service;

import endpoint.config.RabbitMqConfig;
import endpoint.repository.PedidoRepository;
import endpoint.model.PedidoMensagem;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PedidoFilaServiceTest {
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final PedidoRepository repository = mock(PedidoRepository.class);
    private final PedidoFilaService service = new PedidoFilaService(rabbit, repository);

    @Test
    void deveSalvarAntesDePublicar() {
        var id = service.enviar("Pedido #7");
        var ordem = inOrder(repository, rabbit);
        ordem.verify(repository).salvar(new PedidoStatusResponse(id, "Pedido #7", PedidoStatus.PENDING));
        ordem.verify(rabbit).convertAndSend(RabbitMqConfig.FILA_PEDIDOS, new PedidoMensagem(id, "Pedido #7"));
    }

    @Test
    void naoDevePublicarSeGravacaoFalhar() {
        doThrow(new IllegalStateException("Banco indisponivel")).when(repository).salvar(any());
        assertThrows(IllegalStateException.class, () -> service.enviar("Pedido"));
        verifyNoInteractions(rabbit);
    }

    @Test
    void devePropagarFalhaDePublicacao() {
        doThrow(new AmqpException("Fila indisponivel")).when(rabbit)
                .convertAndSend(eq(RabbitMqConfig.FILA_PEDIDOS), any(Object.class));
        assertThrows(AmqpException.class, () -> service.enviar("Pedido"));
        verify(repository).salvar(any());
    }

    @Test
    void deveDelegarProcessamentoEConsulta() {
        var id = UUID.randomUUID();
        service.marcarComoProcessado(id);
        service.buscarPorId(id);
        verify(repository).marcarComoProcessado(id);
        verify(repository).buscarPorId(id);
    }
}
