package endpoint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import endpoint.exception.DivisaoPorZeroException;

class CalculadoraServiceTest {
    private final CalculadoraService calculadora = new CalculadoraService();
    @Test void deveExecutarOperacoesMatematicas() {
        assertEquals(5.0, calculadora.somar(2, 3));
        assertEquals(1.0, calculadora.subtrair(3, 2));
        assertEquals(6.0, calculadora.multiplicar(2, 3));
        assertEquals(2.5, calculadora.dividir(5, 2));
    }
    @Test void deveImpedirDivisaoPorZero() {
        assertThrows(DivisaoPorZeroException.class, () -> calculadora.dividir(10, 0));
    }
}
