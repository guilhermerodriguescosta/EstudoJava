package endpoint.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import endpoint.model.CalculoSalvo;
import endpoint.model.Operacao;
import endpoint.model.PedidoRecebidoResponse;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import endpoint.model.ResultadoOperacao;
import endpoint.exception.NumeroForaDoLimiteException;
import endpoint.service.Calculadora;
import endpoint.service.HistoricoCalculoService;
import endpoint.service.LimiteRequisicoesService;
import endpoint.service.PedidoFilaService;

@RestController
public class EndpointController {
    private static final double LIMITE_NUMERO = 1_000_000;
    private final Calculadora calculadora;
    private final HistoricoCalculoService historicoCalculoService;
    private final LimiteRequisicoesService limiteRequisicoesService;
    private final PedidoFilaService pedidoFilaService;

    public EndpointController(Calculadora calculadora, HistoricoCalculoService historicoCalculoService, LimiteRequisicoesService limiteRequisicoesService, PedidoFilaService pedidoFilaService) {
        this.calculadora = calculadora;
        this.historicoCalculoService = historicoCalculoService;
        this.limiteRequisicoesService = limiteRequisicoesService;
        this.pedidoFilaService = pedidoFilaService;
    }
    @GetMapping("/hello") public String hello() { return "Hello World"; }
    @GetMapping("/somar") public ResultadoOperacao somar(@RequestParam double numero1, @RequestParam double numero2) { return executarOperacao(numero1, numero2, Operacao.SOMA); }
    @GetMapping("/subtrair") public ResultadoOperacao subtrair(@RequestParam double numero1, @RequestParam double numero2) { return executarOperacao(numero1, numero2, Operacao.SUBTRACAO); }
    @GetMapping("/multiplicar") public ResultadoOperacao multiplicar(@RequestParam double numero1, @RequestParam double numero2) { return executarOperacao(numero1, numero2, Operacao.MULTIPLICACAO); }
    @GetMapping("/dividir") public ResultadoOperacao dividir(@RequestParam double numero1, @RequestParam double numero2) { return executarOperacao(numero1, numero2, Operacao.DIVISAO); }
    @GetMapping("/calcular") public ResultadoOperacao calcular(@RequestParam double numero1, @RequestParam double numero2, @RequestParam Operacao operacao) { return executarOperacao(numero1, numero2, operacao); }
    @PostMapping("/historico")
    public ResponseEntity<CalculoSalvo> salvarNoHistorico(Authentication authentication, @RequestParam double numero1, @RequestParam double numero2, @RequestParam Operacao operacao) {
        limiteRequisicoesService.validarCriacao(authentication.getName());
        CalculoSalvo calculoSalvo = historicoCalculoService.salvar(executarOperacao(numero1, numero2, operacao));
        return ResponseEntity.created(URI.create("/historico/" + calculoSalvo.id())).body(calculoSalvo);
    }
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoRecebidoResponse> enviarPedido(@RequestParam String descricao) {
        UUID id = pedidoFilaService.enviar(descricao);
        PedidoRecebidoResponse resposta = new PedidoRecebidoResponse(
                id,
                "Pedido recebido e enviado para processamento.",
                "pedidos",
                descricao,
                PedidoStatus.PENDING
        );
        return ResponseEntity.accepted().body(resposta);
    }
    @GetMapping("/pedidos/{id}")
    public ResponseEntity<PedidoStatusResponse> buscarPedido(@PathVariable UUID id) {
        return pedidoFilaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/historico/{id}")
    public ResponseEntity<CalculoSalvo> buscarNoHistorico(@PathVariable long id) {
        return historicoCalculoService.buscarPorId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/historico/{id}")
    public ResponseEntity<CalculoSalvo> atualizarNoHistorico(@PathVariable long id, @RequestParam double numero1, @RequestParam double numero2, @RequestParam Operacao operacao) {
        ResultadoOperacao resultado = executarOperacao(numero1, numero2, operacao);
        return historicoCalculoService.atualizar(id, resultado).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/historico/{id}")
    public ResponseEntity<Void> removerDoHistorico(@PathVariable long id) {
        return historicoCalculoService.removerPorId(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    private ResultadoOperacao executarOperacao(double numero1, double numero2, Operacao operacao) {
        validarLimiteDosNumeros(numero1, numero2);
        double resultado = switch (operacao) { case SOMA -> calculadora.somar(numero1, numero2); case SUBTRACAO -> calculadora.subtrair(numero1, numero2); case MULTIPLICACAO -> calculadora.multiplicar(numero1, numero2); case DIVISAO -> calculadora.dividir(numero1, numero2); };
        return new ResultadoOperacao(numero1, numero2, operacao, resultado);
    }

    private void validarLimiteDosNumeros(double numero1, double numero2) {
        if (Math.abs(numero1) > LIMITE_NUMERO || Math.abs(numero2) > LIMITE_NUMERO) {
            throw new NumeroForaDoLimiteException();
        }
    }
}
