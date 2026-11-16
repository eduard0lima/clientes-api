package br.com.desafio.clientes.mensageria;

import br.com.desafio.clientes.configuracao.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class PublicadorOutbox {
    private static final Logger log = LoggerFactory.getLogger(PublicadorOutbox.class);

    private final OutboxEventoRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public PublicadorOutbox(OutboxEventoRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${mensageria.outbox.intervalo:2s}")
    @Transactional
    public void publicarPendentes() {
        for (OutboxEvento evento : repository.findTop50ByPublicadoEmIsNullOrderByCriadoEmAsc()) {
            try {
                Message mensagem = MessageBuilder.withBody(evento.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType("application/json")
                        .setMessageId(evento.getId().toString())
                        .build();
                CorrelationData correlacao = new CorrelationData(evento.getId().toString());
                rabbitTemplate.send(RabbitConfig.EXCHANGE_CLIENTES, RabbitConfig.ROTA_CLIENTE_CRIADO, mensagem, correlacao);
                CorrelationData.Confirm confirmacao = correlacao.getFuture().get(3, TimeUnit.SECONDS);
                if (!confirmacao.isAck()) {
                    throw new IllegalStateException("Evento recusado pelo RabbitMQ: " + confirmacao.getReason());
                }
                evento.marcarComoPublicado();
            } catch (Exception excecao) {
                log.warn("Falha ao publicar evento {}. Uma nova tentativa será feita.", evento.getId(), excecao);
                break;
            }
        }
    }
}
