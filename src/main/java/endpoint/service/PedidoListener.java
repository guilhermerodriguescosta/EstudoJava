package endpoint.service;

import endpoint.config.RabbitMqConfig;
import endpoint.model.PedidoMensagem;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PedidoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PedidoListener.class);

    private final PedidoFilaService pedidoFilaService;

    public PedidoListener(PedidoFilaService pedidoFilaService) {
        this.pedidoFilaService = pedidoFilaService;
    }

    @RabbitListener(queues = RabbitMqConfig.FILA_PEDIDOS)
    public void processar(PedidoMensagem pedido) {
        pedidoFilaService.marcarComoProcessado(pedido.id());
        LOGGER.info("Pedido {} recebido da fila {}: {}", pedido.id(), RabbitMqConfig.FILA_PEDIDOS, pedido.descricao());
    }
}
