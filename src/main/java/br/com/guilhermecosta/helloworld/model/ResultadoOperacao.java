package br.com.guilhermecosta.helloworld.model;

public record ResultadoOperacao(
        double numero1,
        double numero2,
        Operacao operacao,
        double resultado) {
}
