package br.com.guilhermecosta.estudojava.exception;

public class LimiteRequisicoesExcedidoException extends RuntimeException {
    public LimiteRequisicoesExcedidoException() {
        super("O usuário limitado pode criar no máximo 3 cálculos por minuto.");
    }
}
