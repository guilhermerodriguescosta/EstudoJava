package br.com.guilhermecosta.helloworld;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraService {

    public int somar(int numero1, int numero2) {
        return numero1 + numero2;
    }

    public int subtrair(int numero1, int numero2) {
        return numero1 - numero2;
    }

    public int multiplicar(int numero1, int numero2) {
        return numero1 * numero2;
    }

    public double dividir(double numero1, double numero2) {
        if (numero2 == 0) {
            throw new IllegalArgumentException("Não é possível dividir por zero.");
        }

        return numero1 / numero2;
    }
}
