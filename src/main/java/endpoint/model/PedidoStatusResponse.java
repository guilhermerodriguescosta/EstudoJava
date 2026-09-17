package endpoint.model;

import java.util.UUID;

public record PedidoStatusResponse(UUID id, String descricao, PedidoStatus status) {
}
