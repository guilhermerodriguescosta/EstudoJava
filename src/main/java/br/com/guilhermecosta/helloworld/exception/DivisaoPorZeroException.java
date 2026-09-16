package br.com.guilhermecosta.helloworld.exception;

public class DivisaoPorZeroException extends RuntimeException {

    public DivisaoPorZeroException() {
        super("Não é possível dividir por zero.");
    }
}
