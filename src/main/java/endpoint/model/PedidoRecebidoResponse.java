package endpoint.model;

import java.util.UUID;

public record PedidoRecebidoResponse(UUID id, String mensagem, String fila, String descricao, PedidoStatus status) {
}
