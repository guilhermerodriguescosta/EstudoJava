package endpoint.repository;

import java.util.Optional;
import java.util.UUID;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import endpoint.persistence.PedidoEntity;
import endpoint.persistence.PedidoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PedidoRepository {
    private final PedidoJpaRepository jpa;

    public PedidoRepository(PedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    // A transacao termina antes de PedidoFilaService publicar no RabbitMQ.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvar(PedidoStatusResponse pedido) {
        jpa.saveAndFlush(new PedidoEntity(pedido));
    }

    @Transactional(readOnly = true)
    public Optional<PedidoStatusResponse> buscarPorId(UUID id) {
        return jpa.findById(id).map(entidade -> entidade.toResponse());
    }

    @Transactional
    public void marcarComoProcessado(UUID id) {
        jpa.atualizarStatus(id, PedidoStatus.PROCESSED);
    }
}
