package br.com.guilhermecosta.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    private final CalculadoraService calculadoraService;

    public HelloWorldController(CalculadoraService calculadoraService) {
        this.calculadoraService = calculadoraService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/somar")
    public int somar(@RequestParam int numero1, @RequestParam int numero2) {
        return calculadoraService.somar(numero1, numero2);
    }

    @GetMapping("/subtrair")
    public int subtrair(@RequestParam int numero1, @RequestParam int numero2) {
        return calculadoraService.subtrair(numero1, numero2);
    }

    @GetMapping("/multiplicar")
    public int multiplicar(@RequestParam int numero1, @RequestParam int numero2) {
        return calculadoraService.multiplicar(numero1, numero2);
    }

    @GetMapping("/dividir")
    public double dividir(@RequestParam double numero1, @RequestParam double numero2) {
        return calculadoraService.dividir(numero1, numero2);
    }
}
