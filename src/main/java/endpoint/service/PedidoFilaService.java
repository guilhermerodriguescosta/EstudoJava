package endpoint.service;

import endpoint.config.RabbitMqConfig;
import endpoint.model.PedidoMensagem;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import endpoint.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PedidoFilaService {

    private final RabbitTemplate rabbitTemplate;
    private final PedidoRepository pedidoRepository;

    public PedidoFilaService(RabbitTemplate rabbitTemplate, PedidoRepository pedidoRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidoRepository = pedidoRepository;
    }

    public UUID enviar(String descricao) {
        UUID id = UUID.randomUUID();
        pedidoRepository.salvar(new PedidoStatusResponse(id, descricao, PedidoStatus.PENDING));
        rabbitTemplate.convertAndSend(RabbitMqConfig.FILA_PEDIDOS, new PedidoMensagem(id, descricao));
        return id;
    }

    public Optional<PedidoStatusResponse> buscarPorId(UUID id) {
        return pedidoRepository.buscarPorId(id);
    }

    public void marcarComoProcessado(UUID id) {
        pedidoRepository.atualizarSeExistir(id,
                pedido -> new PedidoStatusResponse(pedido.id(), pedido.descricao(), PedidoStatus.PROCESSED));
    }
}
