package endpoint.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

import endpoint.model.PedidoStatusResponse;

@Repository
public class PedidoRepository {
    private final ConcurrentMap<UUID, PedidoStatusResponse> pedidos = new ConcurrentHashMap<>();

    public void salvar(PedidoStatusResponse pedido) {
        pedidos.put(pedido.id(), pedido);
    }

    public Optional<PedidoStatusResponse> buscarPorId(UUID id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public void atualizarSeExistir(UUID id, UnaryOperator<PedidoStatusResponse> atualizacao) {
        pedidos.computeIfPresent(id, (chave, pedido) -> atualizacao.apply(pedido));
    }
}
