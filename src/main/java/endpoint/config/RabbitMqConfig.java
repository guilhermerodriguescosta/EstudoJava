package endpoint.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String FILA_PEDIDOS = "pedidos";

    @Bean
    Queue filaPedidos() {
        return new Queue(FILA_PEDIDOS, true);
    }

    @Bean
    MessageConverter pedidoMessageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.addAllowedListPatterns("endpoint.model.*", "java.util.*");
        return converter;
    }
}
