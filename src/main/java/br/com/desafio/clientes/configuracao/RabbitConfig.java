package br.com.desafio.clientes.configuracao;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_CLIENTES = "clientes.eventos";
    public static final String ROTA_CLIENTE_CRIADO = "customer.created";
    public static final String FILA_CLIENTE_CRIADO = "clientes.criado";
    public static final String FILA_ALTERACAO_STATUS = "clientes.status.alteracao";

    @Bean
    TopicExchange exchangeClientes() {
        return new TopicExchange(EXCHANGE_CLIENTES, true, false);
    }

    @Bean
    Queue filaClienteCriado() {
        return QueueBuilder.durable(FILA_CLIENTE_CRIADO).build();
    }

    @Bean
    Binding bindingClienteCriado(Queue filaClienteCriado, TopicExchange exchangeClientes) {
        return BindingBuilder.bind(filaClienteCriado).to(exchangeClientes).with(ROTA_CLIENTE_CRIADO);
    }

    @Bean
    Queue filaAlteracaoStatus() {
        return QueueBuilder.durable(FILA_ALTERACAO_STATUS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_ALTERACAO_STATUS + ".dlq")
                .build();
    }

    @Bean
    Queue filaMortaAlteracaoStatus() {
        return QueueBuilder.durable(FILA_ALTERACAO_STATUS + ".dlq").build();
    }

    @Bean
    Binding bindingAlteracaoStatus(Queue filaAlteracaoStatus, TopicExchange exchangeClientes) {
        return BindingBuilder.bind(filaAlteracaoStatus).to(exchangeClientes).with("customer.status.change");
    }

    @Bean
    Jackson2JsonMessageConverter conversorJson() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RetryOperationsInterceptor repeticaoDoConsumidor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(500, 2, 2_000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter conversorJson,
            RetryOperationsInterceptor repeticaoDoConsumidor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(conversorJson);
        factory.setAdviceChain(repeticaoDoConsumidor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
