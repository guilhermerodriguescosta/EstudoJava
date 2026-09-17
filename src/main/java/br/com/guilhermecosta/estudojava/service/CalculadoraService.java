package br.com.guilhermecosta.estudojava.service;

import org.springframework.stereotype.Service;
import br.com.guilhermecosta.estudojava.exception.DivisaoPorZeroException;

@Service
public class CalculadoraService implements Calculadora {
    public double somar(double numero1, double numero2) { return numero1 + numero2; }
    public double subtrair(double numero1, double numero2) { return numero1 - numero2; }
    public double multiplicar(double numero1, double numero2) { return numero1 * numero2; }
    public double dividir(double numero1, double numero2) {
        if (numero2 == 0) throw new DivisaoPorZeroException();
        return numero1 / numero2;
    }
}
