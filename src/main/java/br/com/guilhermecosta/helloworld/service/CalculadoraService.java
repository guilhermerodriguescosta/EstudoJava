package br.com.guilhermecosta.helloworld.service;

import org.springframework.stereotype.Service;

import br.com.guilhermecosta.helloworld.exception.DivisaoPorZeroException;

@Service
public class CalculadoraService implements Calculadora {

    @Override
    public double somar(double numero1, double numero2) {
        return numero1 + numero2;
    }

    @Override
    public double subtrair(double numero1, double numero2) {
        return numero1 - numero2;
    }

    @Override
    public double multiplicar(double numero1, double numero2) {
        return numero1 * numero2;
    }

    @Override
    public double dividir(double numero1, double numero2) {
        if (numero2 == 0) {
            throw new DivisaoPorZeroException();
        }

        return numero1 / numero2;
    }
}
