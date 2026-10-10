package endpoint.persistence;

import java.util.UUID;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class PedidoEntity {
    @Id
    private UUID id;
    @Column(nullable = false, columnDefinition = "text")
    private String descricao;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PedidoStatus status;

    protected PedidoEntity() {
    }

    public PedidoEntity(PedidoStatusResponse pedido) {
        id = pedido.id();
        descricao = pedido.descricao();
        status = pedido.status();
    }

    public PedidoStatusResponse toResponse() {
        return new PedidoStatusResponse(id, descricao, status);
    }
}
