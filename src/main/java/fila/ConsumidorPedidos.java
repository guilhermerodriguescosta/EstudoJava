package fila;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class ConsumidorPedidos {

    private static final String FILA_PEDIDOS = "pedidos";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("app");
        factory.setPassword("app");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(FILA_PEDIDOS, true, false, false, null);

        DeliverCallback processarPedido = (consumerTag, entrega) -> processarPedido(channel, entrega);

        channel.basicConsume(FILA_PEDIDOS, false, processarPedido, consumerTag -> { });
        System.out.println("Consumidor iniciado. Pressione Ctrl+C para encerrar.");
    }

    static void processarPedido(Channel channel, com.rabbitmq.client.Delivery entrega) throws java.io.IOException {
        String pedido = new String(entrega.getBody(), StandardCharsets.UTF_8);
        System.out.println("Processando: " + pedido);
        channel.basicAck(entrega.getEnvelope().getDeliveryTag(), false);
    }
}
