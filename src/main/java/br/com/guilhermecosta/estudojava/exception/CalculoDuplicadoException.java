package br.com.guilhermecosta.estudojava.exception;

public class CalculoDuplicadoException extends RuntimeException {
    public CalculoDuplicadoException() {
        super("Já existe um cálculo com os mesmos números e operação no histórico.");
    }
}
