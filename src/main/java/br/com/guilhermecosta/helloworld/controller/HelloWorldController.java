package br.com.guilhermecosta.helloworld.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.guilhermecosta.helloworld.model.Operacao;
import br.com.guilhermecosta.helloworld.model.ResultadoOperacao;
import br.com.guilhermecosta.helloworld.service.Calculadora;

@RestController
public class HelloWorldController {

    private final Calculadora calculadora;

    public HelloWorldController(Calculadora calculadora) {
        this.calculadora = calculadora;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/somar")
    public ResultadoOperacao somar(@RequestParam double numero1, @RequestParam double numero2) {
        return executarOperacao(numero1, numero2, Operacao.SOMA);
    }

    @GetMapping("/subtrair")
    public ResultadoOperacao subtrair(@RequestParam double numero1, @RequestParam double numero2) {
        return executarOperacao(numero1, numero2, Operacao.SUBTRACAO);
    }

    @GetMapping("/multiplicar")
    public ResultadoOperacao multiplicar(@RequestParam double numero1, @RequestParam double numero2) {
        return executarOperacao(numero1, numero2, Operacao.MULTIPLICACAO);
    }

    @GetMapping("/dividir")
    public ResultadoOperacao dividir(@RequestParam double numero1, @RequestParam double numero2) {
        return executarOperacao(numero1, numero2, Operacao.DIVISAO);
    }

    @GetMapping("/calcular")
    public ResultadoOperacao calcular(
            @RequestParam double numero1,
            @RequestParam double numero2,
            @RequestParam Operacao operacao) {
        return executarOperacao(numero1, numero2, operacao);
    }

    private ResultadoOperacao executarOperacao(double numero1, double numero2, Operacao operacao) {
        double resultado = switch (operacao) {
            case SOMA -> calculadora.somar(numero1, numero2);
            case SUBTRACAO -> calculadora.subtrair(numero1, numero2);
            case MULTIPLICACAO -> calculadora.multiplicar(numero1, numero2);
            case DIVISAO -> calculadora.dividir(numero1, numero2);
        };

        return new ResultadoOperacao(numero1, numero2, operacao, resultado);
    }
}
