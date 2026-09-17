package endpoint.service;

import endpoint.config.RabbitMqConfig;
import endpoint.model.PedidoMensagem;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PedidoFilaService {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentMap<UUID, PedidoStatusResponse> pedidos = new ConcurrentHashMap<>();

    public PedidoFilaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public UUID enviar(String descricao) {
        UUID id = UUID.randomUUID();
        pedidos.put(id, new PedidoStatusResponse(id, descricao, PedidoStatus.PENDING));
        rabbitTemplate.convertAndSend(RabbitMqConfig.FILA_PEDIDOS, new PedidoMensagem(id, descricao));
        return id;
    }

    public Optional<PedidoStatusResponse> buscarPorId(UUID id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public void marcarComoProcessado(UUID id) {
        pedidos.computeIfPresent(id, (chave, pedido) -> new PedidoStatusResponse(
                pedido.id(), pedido.descricao(), PedidoStatus.PROCESSED));
    }
}
