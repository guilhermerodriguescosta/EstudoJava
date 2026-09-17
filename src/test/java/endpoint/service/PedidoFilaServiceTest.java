package endpoint.service;

import endpoint.config.RabbitMqConfig;
import endpoint.model.PedidoMensagem;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoFilaServiceTest {

    @Test
    void deveEnviarPedidoParaAFilaPedidos() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PedidoFilaService pedidoFilaService = new PedidoFilaService(rabbitTemplate);

        var id = pedidoFilaService.enviar("Pedido #7");

        verify(rabbitTemplate).convertAndSend(RabbitMqConfig.FILA_PEDIDOS, new PedidoMensagem(id, "Pedido #7"));
        assertEquals(PedidoStatus.PENDING, pedidoFilaService.buscarPorId(id).orElseThrow().status());
    }

    @Test
    void deveMarcarPedidoComoProcessado() {
        PedidoFilaService pedidoFilaService = new PedidoFilaService(mock(RabbitTemplate.class));
        var id = pedidoFilaService.enviar("Pedido #9");

        pedidoFilaService.marcarComoProcessado(id);

        PedidoStatusResponse pedido = pedidoFilaService.buscarPorId(id).orElseThrow();
        assertEquals(PedidoStatus.PROCESSED, pedido.status());
        assertTrue(pedidoFilaService.buscarPorId(java.util.UUID.randomUUID()).isEmpty());
    }
}
