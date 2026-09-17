package endpoint.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Service
public class PedidoFilaService {

    private static final String FILA_PEDIDOS = "pedidos";
    private static final AMQP.BasicProperties MENSAGEM_PERSISTENTE =
            new AMQP.BasicProperties.Builder().deliveryMode(2).build();

    private final String host;
    private final int porta;
    private final String usuario;
    private final String senha;

    public PedidoFilaService(
            @Value("${rabbitmq.host}") String host,
            @Value("${rabbitmq.port}") int porta,
            @Value("${rabbitmq.username}") String usuario,
            @Value("${rabbitmq.password}") String senha) {
        this.host = host;
        this.porta = porta;
        this.usuario = usuario;
        this.senha = senha;
    }

    public void enviar(String descricao) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(porta);
        factory.setUsername(usuario);
        factory.setPassword(senha);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(FILA_PEDIDOS, true, false, false, null);
            channel.basicPublish("", FILA_PEDIDOS, MENSAGEM_PERSISTENTE, descricao.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | TimeoutException exception) {
            throw new IllegalStateException("Não foi possível enviar o pedido para o RabbitMQ.", exception);
        }
    }
}
