package fila;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConsumidorPedidosTest {

    @Test
    void deveConfirmarMensagemProcessada() throws Exception {
        Channel channel = mock(Channel.class);
        Delivery entrega = new Delivery(
                new Envelope(42L, false, "", "pedidos"),
                new AMQP.BasicProperties.Builder().build(),
                "Pedido #42".getBytes(StandardCharsets.UTF_8)
        );

        ConsumidorPedidos.processarPedido(channel, entrega);

        verify(channel).basicAck(42L, false);
    }
}
