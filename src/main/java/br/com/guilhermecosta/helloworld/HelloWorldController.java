package br.com.guilhermecosta.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/some")
    public int somar(@RequestParam int numero1, @RequestParam int numero2) {
        return numero1 + numero2;
    }
}
