package endpoint.model;

import java.io.Serializable;
import java.util.UUID;

public record PedidoMensagem(UUID id, String descricao) implements Serializable {
}
