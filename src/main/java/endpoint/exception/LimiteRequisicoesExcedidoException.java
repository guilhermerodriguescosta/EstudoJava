package endpoint.exception;

public class LimiteRequisicoesExcedidoException extends RuntimeException {
    public LimiteRequisicoesExcedidoException() {
        super("O usuário limitado pode criar no máximo 3 cálculos por minuto.");
    }
}
