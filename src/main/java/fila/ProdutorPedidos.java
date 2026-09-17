package fila;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class ProdutorPedidos {

    private static final String FILA_PEDIDOS = "pedidos";
    private static final AMQP.BasicProperties MENSAGEM_PERSISTENTE =
            new AMQP.BasicProperties.Builder().deliveryMode(2).build();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("app");
        factory.setPassword("app");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(FILA_PEDIDOS, true, false, false, null);

            String pedido = "Pedido #1 - notebook";
            channel.basicPublish(
                    "",
                    FILA_PEDIDOS,
                    MENSAGEM_PERSISTENTE,
                    pedido.getBytes(StandardCharsets.UTF_8)
            );

            System.out.println("Pedido enviado: " + pedido);
        }
    }

}
