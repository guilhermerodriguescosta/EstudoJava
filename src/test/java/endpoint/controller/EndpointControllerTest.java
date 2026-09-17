package endpoint.controller;

import endpoint.model.PedidoRecebidoResponse;
import endpoint.model.PedidoStatus;
import endpoint.model.PedidoStatusResponse;
import endpoint.service.Calculadora;
import endpoint.service.HistoricoCalculoService;
import endpoint.service.LimiteRequisicoesService;
import endpoint.service.PedidoFilaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

class EndpointControllerTest {

    @Test
    void deveConfirmarRecebimentoDoPedido() {
        PedidoFilaService pedidoFilaService = mock(PedidoFilaService.class);
        UUID id = UUID.randomUUID();
        when(pedidoFilaService.enviar("Pedido #8")).thenReturn(id);
        EndpointController controller = new EndpointController(
                mock(Calculadora.class),
                mock(HistoricoCalculoService.class),
                mock(LimiteRequisicoesService.class),
                pedidoFilaService
        );

        ResponseEntity<PedidoRecebidoResponse> resposta = controller.enviarPedido("Pedido #8");

        assertEquals(HttpStatus.ACCEPTED, resposta.getStatusCode());
        assertEquals(id, resposta.getBody().id());
        assertEquals("Pedido recebido e enviado para processamento.", resposta.getBody().mensagem());
        assertEquals("pedidos", resposta.getBody().fila());
        assertEquals("Pedido #8", resposta.getBody().descricao());
        assertEquals(PedidoStatus.PENDING, resposta.getBody().status());
        verify(pedidoFilaService).enviar("Pedido #8");
    }

    @Test
    void deveConsultarStatusDoPedido() {
        PedidoFilaService pedidoFilaService = mock(PedidoFilaService.class);
        UUID id = UUID.randomUUID();
        PedidoStatusResponse pedido = new PedidoStatusResponse(id, "Pedido #10", PedidoStatus.PROCESSED);
        when(pedidoFilaService.buscarPorId(id)).thenReturn(Optional.of(pedido));
        EndpointController controller = new EndpointController(mock(Calculadora.class), mock(HistoricoCalculoService.class), mock(LimiteRequisicoesService.class), pedidoFilaService);

        ResponseEntity<PedidoStatusResponse> resposta = controller.buscarPedido(id);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(PedidoStatus.PROCESSED, resposta.getBody().status());
    }
}
